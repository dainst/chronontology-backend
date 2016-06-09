#!/usr/bin/ruby
# encoding: utf-8

require 'rest-client'
require 'json'
require 'csv'
require 'optparse'

################################
#
# Import-Skript für ChronOntology
#
# In der ersten Zeile müssen die Spaltennamen sein, z.B.
# importID,names,types,provenance,definition,description,gazetteer,
#   siblings,parents,senses,timeOriginal,timeStandardized,ongoing,matching,
#   note,linksOrt,linksZeit,linksSTV,tags,note2,note3
#
################################


# Zeilenumbrüche innerhalb von Zellen durch lf ersetzen
# todo: eigentlich gar nicht nötig
lf = " :: "


# Commandline auswerten

# wenn ARGV leer ist, zeige usage summary
ARGV << '-h' if ARGV.empty?

# lies Parameter ein
options = {}
OptionParser.new do |opts|
	opts.banner = "\nUsage: ruby imp-arachne-periods-neu.rb [-i] FILENAME\n\n"
	options[:import] = false
	options[:config] = "config.rb"
	options[:verbose] = false
	opts.on('-i', '--[no-]import', "import the data into the database", "after the conversion from csv to json", "(default is no-import)") do |v|
		options[:import] = v
	end
	opts.on('-c', '--config FILE', "config file with backend URL,", "username and password", "(default filename is config.rb)") do |v|
		options[:config] = v
	end
	opts.on("-v", "--[no-]verbose", "Run verbosely\n\n") do |v|
		options[:verbose] = v
	end
end.parse!

# wenn zwar ein Parameter, aber kein Dateiname angegeben wurde
if (ARGV.empty?) 
	warn "\nNo filename specified. Use -h to show usage summary.\n\n"
	exit
end

# jetzt sollte noch genau ein Dateiname übrig sein
csvFile = ARGV.shift

# wenn nach dem Dateinamen noch weitere Angaben kommen
# TODO: Liste von Dateinamen erlauben?
if (!ARGV.empty?) 
	warn "\nMore than one input file is currently not supported. Use -h to show usage summary.\n\n"
	exit
end

if (options[:import])
	puts "\nechter Import!\n"
else
	puts "\nTestlauf, kein echter Import!\n"
end


# Konfiguration des Backends einlesen

ops = eval(File.open(options[:config]) {|f| f.read })
api_user = ops[:api_user]
api_password = ops[:api_password]
api_url = ops[:api_url]

api = RestClient::Resource.new(api_url, :user => api_user, :password => api_password)


# Positionen der Spalten in der Importtabelle

columnNames = []

# todo: wäre besser nil als -1 ?

columnPos = {

	# pass 1

	"importID" => -1,     # Pflichtfeld, Zeilen ohne ID werden ignoriert
	"names" => -1,        # Pflichtfeld, Fehlen erzeugt Warnung
	"types" => -1,        # Pflichtfeld, Fehlen erzeugt Warnung
	"provenance" => -1,
	"definition" => -1,
	"description" => -1,
	"gazetteer" => -1,    # externe Verknüpfung

	# pass 2

	"siblings" => -1,
	"parents" => -1,
	"senses" => -1,
	"timeOriginal" => -1,
	"timeStandardized" => -1,
	"ongoing" => -1,
	"matching" => -1,
	"note" => -1,
	"linksOrt" => -1,
	"linksZeit" => -1,
	"linksSTV" => -1,
	"tags"  => -1,
	"note2" => -1,       # Arbeitsnotizen
	"note3"  => -1       # Arbeitsnotizen
}


# kontrolliertes Vokabular

namesLanguageList = [

	# moderne Sprachen
	:ar,  # ara Arabisch
	:de,  # deu Deutsch
	:el,  # ell Griechisch
	:en,  # eng Englisch
	:es,  # spa Spanisch
	:eu,  # eus Baskisch
	:fr,  # fra Französisch
	:it,  # ita Italienisch
	:nl,  # nld Niederländisch
	:pl,  # pol Polnisch
	:pt,  # por Portugiesisch
	:ru,  # rus Russisch
	:sq,  # sqi Albanisch
	:sr,  # srp Serbisch
	:tk,  # tuk Türkisch
	:vi,  # vie Vietnamesisch
	:zh,  # zho Chinesisch

	# nicht mehr (weithin) gesprochene Sprachen
	:la,  # lat Latein
	:egy, # Ägyptisch
	:grc, # Altgriechisch
	:xmr  # Meroitisch
]

# Umwandlung ISO 639-1 --> ISO 639-3
# (die technischen Voraussetzungen für ISO 639-3 sind allerdings noch nicht da)
languageList2to3 = {
	:ar => :ara,  # Arabisch
	:de => :deu,  # Deutsch
	:el => :ell,  # Griechisch
	:en => :eng,  # Englisch
	:es => :spa,  # Spanisch
	:eu => :eus,  # Baskisch
	:fr => :fra,  # Französisch
	:it => :ita,  # Italienisch
	:nl => :nld,  # Niederländisch
	:pl => :pol,  # Polnisch
	:pt => :por,  # Portugiesisch
	:ru => :rus,  # Russisch
	:sq => :sqi,  # Albanisch
	:sr => :srp,  # Serbisch
	:tk => :tuk,  # Türkisch
	:vi => :vie,  # Vietnamesisch
	:zh => :zho,  # Chinesisch

	:la => :lat   # lat Latein
}

# Statistik (und gleichzeitig Datenmodell)

intervalBoundaryBlock = {
	:notBefore => 0,      # Datum
	:notAfter => 0,
	:at => 0,
	:atPrecision => 0
}

intervalBlock = { 
	:sourceOriginal => 0,  # freetext
	:sourceURL => 0,
	:timeOriginal => 0,    # freetext
	:calendar => 0,
	:begin => intervalBoundaryBlock.clone,
	:end => intervalBoundaryBlock.clone
}

statistics = {

	# importID
	:externalId => 0,

	# names
	:prefLabel => { :de => 0 },  # deprecated!
	:names => Hash[ namesLanguageList.map {|language| [ language, [ 0 ] ]}],

	# types
	:types => [ 0 ],

	# provenance
	:provenance => 0,

	# definition
	:definition => 0,

	# description
	:description => 0,

	# gazetteer
	:spatiallyPartOfRegion => [ 0 ],
	:hasCoreArea => [ 0 ],
	:namedAfter => [ 0 ],

	# siblings
	:isMetInTimeBy => [ 0 ],    # Allen relation; nicht mehr der default für "kommt nach"
	:meetsInTimeWith => [ 0 ],  # Allen relation; nicht mehr der default für "kommt vor"
	:startsAtTheEndOf => [ 0 ], # fuzzy "kommt nach"
	:endsAtTheStartOf => [ 0 ], # fuzzy "kommt vor"

	# parents
	:isPartOf => [ 0 ],         # by definition
	:hasPart => [ 0 ],          # by definition
	:occursDuring => [ 0 ],     # Allen relation
	:includes => [ 0 ],         # Allen relation

	# senses
	:isSenseOf => [ 0 ],
	:hasSense => [ 0 ],
	
	# timeOriginal
	# timeStandardized
	:hasTimespan => [ intervalBlock.clone ],
	
	# ongoing
	:ongoing => 0,
	
	# matching
	:sameAs => [ 0 ],

	# note
	:note => 0,
	
	# linksOrt  TODO
	# linksZeit TODO
	# linksSTV  TODO
	
	# tags
	:tags => [ 0 ],

	# note2
	:note2 => 0,  # Arbeitsnotiz

	# note3
	:note3 => 0  # Arbeitsnotiz
}


periods = {}
concordance2Chron = {}
concordance2Import = {}

warnings = {}
infos = {}
ignoredRows = []
ignoredRowsReason = []
ignoredColumns = []
ignoredColumnsReason = []


def addWrapper (period)
	periodMitWrapper = {}
	periodMitWrapper["resource"] = period
	periodMitWrapper["dataset"] = "none"
 	return periodMitWrapper
end


# gehe die CSV-Zeilen durch

puts "\n# csv einlesen\n" if options[:verbose]

akzeptierteZeilen = []

zeile = 0
CSV.foreach(csvFile) do |row|
	zeile += 1

# Alternative, falls es nochmal ein Problem mit UTF-8 gibt:
# File.open(csvFile, "r:UTF-8") do |table| 
# 	CSV.parse(table) do |row|
# 		...


	# erste Zeile: bestimme die Spaltenreihenfolge
	if ( columnPos["importID"] == -1 )
		row.each_with_index do |originalName, i|
			name = originalName.to_s.strip
			columnNames[i] = name			
			if (name.length == 0)
				ignoredColumns.push(i.to_s)
				ignoredColumnsReason.push("no column title")
				next
			end
			if (! columnPos.has_key?(name) )
				ignoredColumns.push(i.to_s+name)
				ignoredColumnsReason.push("not a recognized column title")				
				next
			end
			if (columnPos[name] > -1)
				ignoredColumns.push(i.to_s+name)
				ignoredColumnsReason.push("column title already exists")
				next
			end
			columnPos[name] = i
		end
		if (columnPos["importID"] == -1)
			warn('Spalte "importID" fehlt!')
			exit
		end
		if (columnPos["names"] == -1)
			warn('Spalte "names" fehlt!')
			exit
		end
		if (columnPos["types"] == -1)
			warn('Spalte "types" fehlt!')
			exit
		end
		next
	end

	# ignoriere alle Zeilen ohne ID
	if (row[columnPos["importID"]].to_s.strip.length == 0)

		# strukturierende Leerzeilen stillschweigend ignorieren
		next if ( row.join("").match(/^\s*$/) )

		# ansonsten auch ignorieren, aber nicht stillschweigend
		ignoredRows.push(row)
		ignoredRowsReason.push("no ID; row "+zeile.to_s)
		next
	end

	# ignoriere Zeilen, deren ID schon vorgekommen ist 
	# (kann auftreten, wenn mehrere Tabellen hintereinander eingelesen werden)
	# TODO: anders lösen
	if ( warnings.key?(row[columnPos["importID"]]) )
		ignoredRows.push(row)
		ignoredRowsReason.push("duplicate ID; row "+zeile.to_s)
		next
	end


	# ignoriere Problemzeilen
	# 1. AAT-Zeilen wie "römische Keramikstile"
	# TODO doch als parent behalten, oder unnötig?
# 		if ( row[columnPos["types"]].to_s.strip.match(/alle Bedeutungen\?\?/) )
# 
# 			ignoredRows.push(row)
# 			ignoredRowsReason.push("superfluous AAT node")
# 			next
# 		end

	# ersetze Zeilenumbrüche innerhalb von Zellen
	rowBearbeitet = []
	row.each do |cell|
		rowBearbeitet.push((cell || "").gsub(/\n/, lf))
	end
	akzeptierteZeilen.push(rowBearbeitet)

#	akzeptierteZeilen.push(row)

	# ruby hat's gern explizit initialisiert
	warnings[ row[columnPos["importID"]] ] = []
	infos[ row[columnPos["importID"]] ] = []
end


# Spalten, die sich nicht auf andere Einträge in chronontology beziehen
# (inkl. Verweise auf den Gazetteer)

puts "\n# csv --> json, Teil 1\n" if options[:verbose]

akzeptierteZeilen.each do |row|

	period = {}

	# Spalte: importID
	# (oben wurde bereits sichergestellt, dass es eine ID gibt)
	# aat:300019255
	# G0001

	importID = row[columnPos["importID"]]

	if (importID.match(':'))
		period[:externalId] = importID
		statistics[:externalId] += 1

#		if ( importID.match('aat:') )
#			period[:sameAs] = "http://vocab.getty.edu/" + importID.gsub(/:/, '/')
#			statistics[:sameAs] += 1
#		end

	end


	# Spalte: names
	# Early Western World@en
	# Mediterranean@en (pref.) :: Mediterranean (Early Western World)@en
	# TODO: part of :: 2181124 (Mittelmeerraum), :: 2043065 (Vorderasien), :: 2044223 (Europe)

	if (columnPos["names"] > -1)
		if (row[columnPos["names"]].to_s.strip.length == 0)
			warnings[importID].push("Pflichtfeld Name fehlt")
		end

		names = {}
		prefVorhanden = {}

		einzelneNamen = row[columnPos["names"]].split(lf)
		einzelneNamen.each do |nameMitSprache|
			if ( !nameMitSprache.match(/^(.+?)@([a-z]+)/) )
				warnings[importID].push("kann Namen nicht erkennen: "+nameMitSprache)
				next
			end
			name = $1
			sprachkuerzel = $2.downcase.to_sym
#			if ( languageList2to3.include? sprachkuerzel )
#				sprachkuerzel = languageList2to3[sprachkuerzel]
#			end

			# alt: prefLabel, altLabel, quick'n'dirty, kommt sowieso weg
			# ignoriert sprachkuerzel, und erzeugt keine AltLabel

			if (!period.has_key?("prefLabel"))
				period[:prefLabel] = { :de => name }
				statistics[:prefLabel][:de] += 1
			end

			# neu: names mit name, language
			# erster Eintrag in jeder Sprache ist preferred:
			#   explizite preferred names kommen an den Anfang der Liste

			if (! namesLanguageList.include? sprachkuerzel )
				warnings[importID].push("Sprachkürzel nicht erkannt: "+nameMitSprache)
				next
			end

			names[sprachkuerzel] ||= []		
			if ( nameMitSprache.match(/\(pref.\)/) )
				if ( prefVorhanden[sprachkuerzel] )
					warnings[importID].push("mehrfaches pref in einer Sprache: "+nameMitSprache)
					names[sprachkuerzel].push(name)
				else
					names[sprachkuerzel].unshift(name)
					prefVorhanden[sprachkuerzel] = true
				end
			else
				names[sprachkuerzel].push(name)
			end

			position = names[sprachkuerzel].length - 1
			statistics[:names][sprachkuerzel][position] ||= 0
			statistics[:names][sprachkuerzel][position] += 1
		end
		period[:names] = names
	end


	# Spalte: types
	# TODO: Whitespace entfernen
	# TODO: mit Vokab abgleichen

	if (columnPos["types"] > -1)
		if (row[columnPos["types"]].to_s.strip.length == 0)
			warnings[importID].push("Pflichtfeld Type fehlt")
		end
		
		typefeld = row[columnPos["types"]]
		types = typefeld.gsub(/, ?/, lf).split(lf)

		typesStandardized = []
		types.each_with_index do |type, i|

			# Normalfall:
			# "alle Bedeutungen"
			# "kulturell"
			
			typeStandardized = type

			
			# Ausnahme (und hack) für Problemzeilen:
			if ( type.match(/alle Bedeutungen\?\?/) )
				typeStandardized = "list"
			elsif ( type.match(/^ *title *$/) )
				typeStandardized = "list"
			end
			
			# Hack für geologische Types:
			# Geological: Eon --> Geological Eon
			if ( type.match(/Geological:/) )
				typeStandardized.gsub!(/Geological:/, 'Geological')
			end
			
			
			# "material culture: pottery"
			# --> entferne die Hierarchie
			if (typeStandardized.match(/: ?(.+)/) )
				typeStandardized = $1
			end

			# "(material culture:) pottery"
			# --> ergänze "style"
			# TODO: in der Tabelle ändern
			if (typeStandardized.strip.match(/^pottery$/) )
				typeStandardized = "pottery style"
			end	

			typesStandardized.push(typeStandardized)

			statistics[:types][i] ||= 0
			statistics[:types][i] += 1
		end
		period[:types] = typesStandardized
	end


	# Spalte: provenance
	# Getty

	if (columnPos["provenance"] > -1)
		provenance = row[columnPos["provenance"]]

		if (provenance == "Getty")
			provenance = "aat"
		end
		# todo: rückübertragen in die Tabellen und hier rausnehmen

		if (provenance.to_s.strip.length == 0)
			provenance = "chronontology"
			infos[importID].push('keine provenance angegeben; "chronontology" ergänzt')
		end
		period[:provenance] = provenance
		statistics[:provenance] += 1
	end


	# Spalte: definition
	# (Freitext; zurzeit in der Tabelle leer)

	if (columnPos["definition"] > -1)
		definition = row[columnPos["definition"]]
		if (definition.to_s.strip.length > 0)
			period[:definition] = definition
			statistics[:definition] += 1
		end
	end


	# Spalte: description
	# (Freitext)

	if (columnPos["description"] > -1)
		description = row[columnPos["description"]]
		if (description.to_s.strip.length > 0)
			period[:description] = description
			statistics[:description] += 1
		end
	end


	# Spalte: gazetteer
	# 2309807 (Italien)
	# part of :: 2181124 (Mittelmeerraum), :: 2043065 (Vorderasien), :: 2044223 (Europe)
	# 2181124, 2043065, 2044223
	# TODO: Kerngebiet, namedAfter

	if (columnPos["gazetteer"] > -1)
		gazetteerfeld = row[columnPos["gazetteer"]]
		if (gazetteerfeld.to_s.strip.length > 0)

			gazetteerIDs = []
			gazetteerIDsPlusText = gazetteerfeld.gsub(/, ?/, lf).split(lf)

			i = -1
			gazetteerIDsPlusText.each do |gazetteerIdPlusText|
				if ( gazetteerIdPlusText.match(/^([0-9]+)(.*)$/) )
					i += 1
					gazetteerIDs.push("http://gazetteer.dainst.org/place/" + $1)
					if ($2.to_s.strip.length > 0)
						infos[importID].push("Gazetteer-ID Text teilweise ignoriert: "+gazetteerIdPlusText+" --> "+$1 )
					end
					statistics[:spatiallyPartOfRegion][i] ||= 0
					statistics[:spatiallyPartOfRegion][i] += 1

				# "(erbt)"
				elsif ( gazetteerIdPlusText.match(/^ *\(erbt\) *$/) )
					infos[importID].push("Gazetteer-Eintrag wurde ignoriert: "+gazetteerIdPlusText)					
				else
					warnings[importID].push("Gazetteer-ID nicht erkannt: "+gazetteerIdPlusText)
				end
			end
			if (gazetteerIDs.count > 0)
				period[:spatiallyPartOfRegion] = gazetteerIDs
			end
		end
	end

	
	# Spalte: tags

	if (columnPos["tags"] > -1)	
		tagsfeld = row[columnPos["tags"]]
		tags = tagsfeld.gsub(/, ?/, lf).split(lf)

		tags.each_with_index do |tag, i|
			period[:tags][i] = tag
			statistics[:tags][i] ||= 0
			statistics[:tags][i] += 1
		end
	end

	# Spalten: note, note2, note3
	# note wird in der GUI angezeigt, note2 und note3 sind interne Arbeitsnotizen

	if (columnPos["note"] > -1)
		note = row[columnPos["note"]]
		if (note.to_s.strip.length > 0)
			period[:note] = note
			statistics[:note] += 1
		end
	end

	if (columnPos["note2"] > -1)
		note2 = row[columnPos["note2"]]
		if (note2.to_s.strip.length > 0)
			period[:note2] = note2
			statistics[:note2] += 1
		end
	end

	if (columnPos["note3"] > -1)
		note3 = row[columnPos["note3"]]
		if (note3.to_s.strip.length > 0)
			period[:note3] = note3
			statistics[:note3] += 1
		end
	end


	# Spalte: ongoing

	if (columnPos["ongoing"] > -1)	
		ongoingfeld = row[columnPos["ongoing"]].to_s.strip

		if ( ongoingfeld.length > 0 )
			if ( ongoingfeld.eql?("ja") )
				period[:ongoing] = true
				statistics[:ongoing] += 1
			else
				warnings[importID].push("ongoing wurde nicht erkannt: "+ongoingfeld)
			end
		end
	end
	

	# Spalte: timeOriginal
	# Spalte: timeStandardized
	# --> der Inhalt von zwei Spalten kommt in dieselbe Struktur!
	# TODO: sourceOriginal

	if (columnPos["timeOriginal"] > -1)
		timeOriginal = row[columnPos["timeOriginal"]].to_s.strip
		if (timeOriginal.length > 0)
			
			# ad-hoc-Initialisierung
			# TODO: umgehen mit mehr als einer Zeitangabe
			period[:hasTimespan] ||= []
			period[:hasTimespan][0] ||= {}

			# ... (source ...)
			if ( timeOriginal.match(/^(.+) +\(source (.+)\) *$/) )
				source = $2
				timeOriginal = $1
				period[:hasTimespan][0][:sourceOriginal] = source
				statistics[:hasTimespan][0][:sourceOriginal] += 1
			end

			period[:hasTimespan][0][:timeOriginal] = timeOriginal
			statistics[:hasTimespan][0][:timeOriginal] += 1
			
			# ohne timeOriginal kein timeStandardized
			# TODO: das mag sich ändern
			if (columnPos["timeStandardized"] > -1)

				timeStandardizedFeld = row[columnPos["timeStandardized"]].to_s.strip
				if (timeStandardizedFeld.length > 0)

					# [+235; +284]
					# [+250;+ 300] --> TODO: in der Tabelle korrigieren?
					# [?;?]
					# [+170, +192]
					# [+69,?]
					# TODO regex lesbarer machen
					if ( timeStandardizedFeld.match(/^ *\[((?:[+-]? ?[0-9]+)|\?|unknown)[;,] ?((?:[+-]? ?[0-9]+)|\?|unknown)\] *$/) )
						from = $1
						to = $2
#						puts timeStandardizedFeld+" <"+from+"> <"+to+">"
						if ( from.to_s.match(/^\+/) )
							from = from.to_s.gsub(/^\+/, '')
						end
						if ( to.to_s.match(/^\+/) )
							to = to.to_s.gsub(/^\+/, '')
						end
						
						timespan = period[:hasTimespan][0]
						timespan[:begin] = {}
						timespan[:end] = {}
						
						timespan[:begin][:at] = from
						statistics[:hasTimespan][0][:begin][:at] += 1
						if ( (! from.eql?("?")) && (! from.eql?("unknown")) ) 
							timespan[:begin][:atPrecision] = "ca"
							statistics[:hasTimespan][0][:begin][:atPrecision] += 1
						end
						
						timespan[:end][:at] = to
						statistics[:hasTimespan][0][:end][:at] += 1
						if ( (! to.eql?("?")) && (! to.eql?("unknown")) )
							timespan[:end][:atPrecision] = "ca"
							statistics[:hasTimespan][0][:end][:atPrecision] += 1
						end

					# [-1800000; ] mit ongoing = true
					# ( [?; ] wird absichtlich nicht erkannt; kommt das jemals vor?)
					elsif ( timeStandardizedFeld.match(/^\[([+-]?[0-9]+)[;,] ?\]$/) and period[:ongoing] )
						from = $1 
						if ( from.to_s.match(/^\+/) )
							from = from.to_s.gsub(/^\+/, '')
						end
						timespan = period[:hasTimespan][0]
						timespan[:begin] = {}
						timespan[:begin][:at] = from
						timespan[:begin][:atPrecision] = "ca"
						statistics[:hasTimespan][0][:begin][:at] += 1
						statistics[:hasTimespan][0][:begin][:atPrecision] += 1
					else
						warnings[importID].push("timeStandardized wurde nicht erkannt: "+timeStandardizedFeld)
					end
				end
			end
		end
	end


	# ersten Teil abschließen

	periods[importID] = period

	if (! options[:import])
		# ohne den Import gibt es keine chronontologyIDs; fülle trotzdem die
		# Konkordanztabellen, um später keine Fallunterscheidung machen zu müssen
		concordance2Chron[importID] = importID
		concordance2Import[importID] = importID
	end
end


# ersten Teil importieren, um chronontology-IDs zu erhalten

if (options[:import])

	puts "\n# json importieren, Teil 1\n" if (options[:verbose])

	akzeptierteZeilen.each do |row|

		importID = row[columnPos["importID"]]
		period = periods[importID]

		response = api["period/"].post(addWrapper(period).to_json, :content_type => :json, :accept => :json)

		chronontologyID = response.headers[:location]
		concordance2Chron[importID] = chronontologyID
		concordance2Import[chronontologyID] = importID

		if (options[:verbose])
			puts "\n"
			puts chronontologyID

# TODO: in `encode': "\xC3" from ASCII-8BIT to UTF-8 (Encoding::UndefinedConversionError)
# wegen der folgenden Zeile
#			puts response.to_json.gsub(/\\"/, '"')
		end
	end
end


# Spalten, die sich auf andere Einträge in chronontology beziehen

puts "\n# csv --> json, Teil 2\n" if options[:verbose]

akzeptierteZeilen.each do |row|

	importID = row[columnPos["importID"]]
	period = periods[importID]


	addIDs = lambda do |propertyIDs, symbol|
		if (propertyIDs.count > 0)
			propertyIDs.each do |property|
				if (concordance2Chron.has_key?(property))
					period[symbol] ||= []
					period[symbol].push(concordance2Chron[property])
					i = period[symbol].count -1
					statistics[symbol][i] ||= 0
					statistics[symbol][i] += 1
				else
					warnings[importID].push("Verweis auf nicht vorhandene ID "+symbol.to_s+" "+property)
				end
			end
		end			
	end
	

	# Spalte: siblings
	# TODO: bisher alles keine Listen; muss man ändern, wenn es Beispiele gibt 

	if (columnPos["siblings"] > -1)
		siblingFeld = row[columnPos["siblings"]].to_s.strip

		isMetInTimeByIDs = []
		meetsInTimeWithIDs = []
		startsAtTheEndOfIDs = []
		endsAtTheStartOfIDs = []
	
		if (siblingFeld.length > 0)
		
			# A0021 (comes before)
			# aat:300107341 (comes before)
			if ( siblingFeld.match(/^ *([a-zA-Z:0-9]+) +\(comes before\) *$/) )
				meetsInTimeWithIDs.push($1)
				endsAtTheStartOfIDs.push($1)
			# aat:300107343 (comes after)
			elsif ( siblingFeld.match(/^ *([a-zA-Z:0-9]+) +\(comes after\) *$/) )
				isMetInTimeByIDs.push($1)
				startsAtTheEndOfIDs.push($1)
			# A0077 (comes after) A0089 (comes before)
			elsif ( siblingFeld.match(/^ *([a-zA-Z:0-9]+) +\(comes after\) *([a-zA-Z:0-9]+) +\(comes before\) *$/) )
				isMetInTimeByIDs.push($1)
				startsAtTheEndOfIDs.push($1)
				meetsInTimeWithIDs.push($2)
				endsAtTheStartOfIDs.push($2)
			else
				warnings[importID].push("comes before/after wurde nicht erkannt: "+siblingFeld)
			end


			# Allen-Relationen			
			addIDs.call(isMetInTimeByIDs, :isMetInTimeBy)
			addIDs.call(meetsInTimeWithIDs, :meetsInTimeWith)
				
			# fuzzy Relationen
			addIDs.call(startsAtTheEndOfIDs, :startsAtTheEndOf)
			addIDs.call(endsAtTheStartOfIDs, :endsAtTheStartOf)
		end
	end

	
	# Spalte: parents
	# probehalber werden, abweichend vom Gazetteer, auch die Kinder explizit aufgelistet
	# (nicht so lange Listen wie im Gazetteer, und mehrere Hierarchien)

	if (columnPos["parents"] > -1)
		parentsFeld = row[columnPos["parents"]].to_s.strip

		isPartOfIDs = []
		hasPartIDs = []

		if (parentsFeld.length > 0)

			# G0005 --> ist enthalten
			if ( parentsFeld.match(/^ *([a-zA-Z:0-9]+) *$/) )
				isPartOfIDs.push($1)
			# A0081 (is part of)
			# aat:300020533 (is part of) --> ist enthalten
			elsif ( parentsFeld.match(/^ *([a-zA-Z:0-9]+) *\(is part of\) *$/) )
				isPartOfIDs.push($1)
			# aat:300020666; aat:300020541 (is part of) --> enthält, ist enthalten
			elsif ( parentsFeld.match(/^ *([a-zA-Z:0-9]+) *; *([a-zA-Z:0-9]+) *\(is part of\) *$/) )
				hasPartIDs.push($1)
				isPartOfIDs.push($2)
			else
				warnings[importID].push("isPartOf/hasPart wurde nicht erkannt: "+parentsFeld)
			end

			addIDs.call(isPartOfIDs, :isPartOf)
			addIDs.call(hasPartIDs, :hasPart)
		end
	end


	# Spalte: senses
	# beachte: isSenseOf ist eine Liste, aber das Skript geht vorerst trotzdem 
	#   von einem einzelnen Wert in der Importtabelle aus

	if (columnPos["senses"] > -1)
		sensesFeld = row[columnPos["senses"]].to_s.strip
		if (sensesFeld.length > 0)
		
			isSenseOfIDs = []

			# "A0105, in sense of"
			if ( sensesFeld.match(/^ *([a-zA-Z:0-9]+), in sense of *$/) )
				isSenseOfIDs.push($1)
			else
				warnings[importID].push("sense wurde nicht erkannt: "+sensesFeld)
			end

			addIDs.call(isSenseOfIDs, :isSenseOf)
		end
	end


	# Spalte: matching
	# beachte: sameAs ist eine Liste, aber das Skript geht vorerst trotzdem 
	#   von einem einzelnen Wert in der Importtabelle aus

	if (columnPos["matching"] > -1)
		matchingFeld = row[columnPos["matching"]].to_s.strip
		if (matchingFeld.length > 0)
		
			matchingIDs = []
		
			# sameAs G0008 --> Standardform
			if ( matchingFeld.match(/^ *sameAs +([a-zA-Z:0-9]+) *$/) )
				matchingIDs.push($1)
			# G0001, sameAs --> Variation
			elsif ( matchingFeld.match(/^ *([a-zA-Z:0-9]+), sameAs *$/) )
				matchingIDs.push($1)
			# G0001, Def/temporal :: sameAs aat:300020058 --> ersten Teil erstmal ignorieren
			elsif ( matchingFeld.match(/ *sameAs +([a-zA-Z:0-9]+) *$/)  )
				matchingIDs.push($1)
			else
				warnings[importID].push("matching wurde nicht erkannt: "+matchingFeld)
			end

			addIDs.call(matchingIDs, :sameAs)
		end
	end

end


# inverse Properties eintragen

puts "\n# inverse Properties ergänzen\n" if options[:verbose]

akzeptierteZeilen.each do |row|

	importID = row[columnPos["importID"]]
	chronontologyID = concordance2Chron[importID]
	period = periods[importID]
	
	complement = lambda do |normal, inverse|	
		if ( period.has_key?(normal) )
			period[normal].each do |normal|
				counterpartImportID = concordance2Import[ normal ]
				counterpart = periods[ counterpartImportID ]

				if ( counterpart.has_key?(inverse) )
					if (! counterpart[inverse].include?( chronontologyID ) )
						counterpart[inverse].push(chronontologyID)
						infos[counterpartImportID].push(inverse.to_s+" "+chronontologyID+" ergänzt")

						i = counterpart[inverse].count - 1
						statistics[inverse][i] ||= 0
						statistics[inverse][i] += 1
					end
				else
					counterpart[inverse] = [ chronontologyID ]
					infos[counterpartImportID].push(inverse.to_s+" "+chronontologyID+" ergänzt")
					statistics[inverse][0] += 1
				end
			end
		end
	end
	
	complement.call(:isMetInTimeBy, :meetsInTimeWith)
	complement.call(:meetsInTimeWith, :isMetInTimeBy)

	complement.call(:startsAtTheEndOf, :endsAtTheStartOf)
	complement.call(:endsAtTheStartOf, :startsAtTheEndOf)

	complement.call(:sameAs, :sameAs)       # sameAs ist symmetrisch
	
	complement.call(:isSenseOf, :hasSense)
	complement.call(:hasSense, :isSenseOf)  # macht zurzeit noch nichts

	complement.call(:isPartOf, :hasPart)
	complement.call(:hasPart, :isPartOf)
end


# Reasoning: abgeleitete Allen relations eintragen
# TODO sobald das System das selbst kann, kann es hier weg?
# TODO: sameAs ist transitiv, es müssten also evtl. noch mehr sameAs ergänzt werden

puts "\n# abgeleitete Allen relations ergänzen\n" if options[:verbose]

akzeptierteZeilen.each do |row|

	importID = row[columnPos["importID"]]
	period = periods[importID]

	# beachte: zurzeit gibt es occursDuring/includes nur als abgeleitete Relationen
	# TODO prüfen, ob Allen relation schon vorhanden ist
	# TODO Allen relation auch eigenständig eintragen

	reasoning = lambda do |known, inferred|
		if ( period.has_key?(known) )
			period[inferred] = period[known].clone
			statistics[inferred] = statistics[known].clone
			infos[importID].push("ein oder mehrere abgeleitete #{inferred} ergänzt")
		end		
	end
	
	reasoning.call(:isPartOf, :occursDuring)
	reasoning.call(:hasPart, :includes)
end


# neue Felder importieren

if (options[:import])

	puts "\n# json importieren, Teil 2\n" if options[:verbose]

	akzeptierteZeilen.each do |row|

		importID = row[columnPos["importID"]]
		chronontologyID = concordance2Chron[importID]
		period = periods[importID]

		api[chronontologyID].put(addWrapper(period).to_json, :content_type => :json, :accept => :json)

		if (options[:verbose])
			puts "\n"
			puts importID
			puts chronontologyID
			puts addWrapper(period).to_json
		end
	end
end


# Ergebnis

puts "\n# Ergebnis\n" if options[:verbose]

if (options[:verbose])

	# Statistik
	puts "\nStatistik:\n"
	puts JSON.pretty_generate(statistics)

	# ignorierte Spalten
	puts "\n"
	ignoredColumns.each_with_index do |ignoredColumn, i|
		puts "IGNORED COLUMN (reason: "+ignoredColumnsReason[i]+") "+ignoredColumn
	end

	# ignorierte Zeilen
	puts "\n"
	ignoredRows.each_with_index do |ignoredRow, i|
		ignoredRowMsg = "IGNORED ROW (reason: "+ignoredRowsReason[i]+")"
		ignoredRow.each_with_index do |zelle, i|
			if (zelle.to_s.strip.length > 0)
				ignoredRowMsg += " #{i} #{columnNames[i]}: #{zelle}"
			end
		end
		puts ignoredRowMsg
	end
end

akzeptierteZeilen.each do |row|

	importID = row[columnPos["importID"]]
	chronontologyID = concordance2Chron[importID]

	puts "\n"
	if (options[:verbose])
		row.each_with_index do |zelle, i|
			if (zelle.to_s.strip.length > 0)
				puts "#{i} #{columnNames[i]}: #{zelle}"
			end
		end
	end
	puts importID if !options[:verbose]
	puts chronontologyID if options[:import]

#	puts periods[importID].to_json
	puts JSON.pretty_generate(periods[importID])

	# Warnungen
	warnings[importID].each do |warnung|
			puts "WARNUNG "+importID+": "+warnung
	end

	# Infos
	if (options[:verbose])
		infos[importID].each do |info|
				puts "INFO "+importID+": "+info
		end
	end
end


# TODO: Skript anpassen, dass es auch mit Verweisen auf bereits vorhandene 
#       Periods umgehen kann

# TODO: "preferred" Einträge in den Datenfeldern, z.B. "comes before ..., comes after: ...

# TODO: besseres stderr bei Skript-Abbruch durch Fehler

# TODO: nicht zeilenweise, sondern spaltenweise vorgehen?

