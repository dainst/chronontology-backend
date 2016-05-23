#!/usr/bin/ruby
# encoding: utf-8

require 'rest-client'
require 'json'
require "csv"
require 'optparse'

################################

# Import-Skript für ChronOntology
#
# Das Skript erwartet eine csv-Datei, in der 
# Zeilenumbrüche innerhalb von Zellen durch " :: " ersetzt sind
# --> csv-korrektur.pl
#
# In der ersten Zeile müssen die Spaltennamen sein, z.B.
# importID,names,types,provenance,definition,description,gazetteer,
#   siblings,parents,senses,timeOriginal,timeStandardized,ongoing,matching,
#   notes,linksOrt,linksZeit,linksSTV,tags,,notes2,,notes3


lf = " :: "

# TODO: ist dieses preprocessing überhaupt nötig?

################################

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

################################

ops = eval(File.open(options[:config]) {|f| f.read })
api_user = ops[:api_user]
api_password = ops[:api_password]
api_url = ops[:api_url]

api = RestClient::Resource.new(api_url, :user => api_user, :password => api_password)

################################

columnNames = []

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
	"notes" => -1,
	"linksOrt" => -1,
	"linksZeit" => -1,
	"linksSTV" => -1,
	"tags"  => -1,
	"notes2" => -1,       # Arbeitsnotizen
	"notes3"  => -1       # Arbeitsnotizen
}


# Statistik (und gleichzeitig Datenmodell)
# todo: Statistik für kontrolliertes Vokabular

namesBlock = { :name => 0, :language => 0, :pref => 0 }

timeBlock = { :timeOriginal => 0, :source => 0, :from => 0, :to => 0 }

statistics = {

	# importID
	:externalId => 0,

	# names
	:prefLabel => { :de => 0 },  # deprecated!
	:names => [ namesBlock.clone ],

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
	:namedAfter => 0,

	# siblings
	:isMetInTimeBy => 0,  # nicht deprecated, aber nicht mehr der default für "kommt nach"
	:meetsInTimeWith => 0,  # nicht deprecated, aber nicht mehr der default für "kommt vor"
	:startsAtTheEndOf => 0, # fuzzy "kommt nach"
	:endsAtTheStartOf => 0, # fuzzy "kommt vor"

	# parents
	:fallsWithin => 0,
	:contains => [ 0 ],

	# senses
	:isSenseOf => 0,
	:hasSense => [ 0 ],
	
	# timeOriginal
	# timeStandardized
	# :hasTimespan => [ { :timeOriginal => 0, :source => 0, :from => 0, :to => 0 } ],
	:hasTimespan => [ timeBlock.clone ],
	
	# ongoing
	:ongoing => 0,
	
	# matching
	:sameAs => 0,

	# notes
	# linksOrt
	# linksZeit
	# linksSTV
	# tags
	# notes2
	# notes3
	
	:dummy => 0
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


# TODO: csv-Datei muss als echte UTF-8-Datei eingelesen werden!

# alleZeilen = CSV.read(csvFile, col_sep: "$", encoding: "UTF-8") 
# alleZeilen.each do |row|
# --> funktioniert nicht

# file_content = File.open("test.txt", "r:UTF-8", &:read)
# --> noch nicht ausprobiert

# CSV.foreach(csvFile) do |row|


File.open(csvFile, "r:UTF-8") do |table| 
	CSV.parse(table) do |row|
# --> funktioniert das jetzt?

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
			next
		end

		# ignoriere alle Zeilen ohne ID
		if (row[columnPos["importID"]].to_s.strip.length == 0)

			# strukturierende Leerzeilen stillschweigend ignorieren
			next if ( row.join("").match(/^\s*$/) )

			# ansonsten auch ignorieren, aber nicht stillschweigend
			ignoredRows.push(row)
			ignoredRowsReason.push("no ID")
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

		akzeptierteZeilen.push(row)

		# ruby hat's gern explizit initialisiert
		warnings[ row[columnPos["importID"]] ] = []
		infos[ row[columnPos["importID"]] ] = []
	end
end


# Spalten, die sich nicht auf andere Einträge beziehen

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

		names = []

		prefVorhanden = {}
		spracheVorhanden = {}

		einzelneNamen = row[columnPos["names"]].split(lf)
		einzelneNamen.each do |nameMitSprache|
			if (nameMitSprache.match(/@([a-z]+) +\(pref.\)/) )
				prefVorhanden[ $1 ] = true
			end
		end
		einzelneNamen.each_with_index do |nameMitSprache, i|
			if ( !nameMitSprache.match(/^(.+?)@([a-z]+)/) )
				warnings[importID].push("kann Namen nicht erkennen: "+nameMitSprache)
			end
			name = $1
			sprachkuerzel = $2

			# alt: prefLabel, altLabel, quick'n'dirty, kommt sowieso weg
			# ignoriert sprachkuerzel, und erzeugt keine AltLabel

			if (!period.has_key?("prefLabel"))
				period[:prefLabel] = { :de => name }
				statistics[:prefLabel][:de] += 1
			end

			# neu: names mit name, language, pref

			# pref, falls:
			# 1. explizit angegeben
			# 2. keine pref für diese Sprache angegeben,
			#    und erster (oder einziger) Name in dieser Sprache
			pref = false
			if ( nameMitSprache.match(/\(pref.\)/) )
				pref = true
			elsif ( !prefVorhanden.has_key?(sprachkuerzel) && !spracheVorhanden.has_key?(sprachkuerzel) )
				pref = true
			end
			spracheVorhanden[sprachkuerzel] = 1

			names.push( {
				:name => name,
				:language => sprachkuerzel,
				:pref => pref
			} )

			statistics[:names][i] ||= namesBlock.clone
			statistics[:names][i][:name] += 1
			statistics[:names][i][:language] += 1
			statistics[:names][i][:pref] += 1 if pref
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

		typesOhneHierarchie = []
		types.each_with_index do |type, i|

			# Ausnahme (und hack) für Problemzeilen:
			if ( type.match(/alle Bedeutungen\?\?/) )
				typesOhneHierarchie.push("Strukturknoten")
			elsif ( type.match(/^ *title *$/) )
				typesOhneHierarchie.push("Strukturknoten")
				
			# material culture: pottery
			elsif (type.match(/: ?(.+)/) )
				typesOhneHierarchie.push($1)
			# alle Bedeutungen
			# kulturell
			else
				typesOhneHierarchie.push(type)
			end

			statistics[:types][i] ||= 0
			statistics[:types][i] += 1
		end
		period[:types] = typesOhneHierarchie
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
				if (gazetteerIdPlusText.match(/^([0-9]+)(.*)$/) )
					i += 1
					gazetteerIDs.push("http://gazetteer.dainst.org/place/" + $1)
					if ($2.to_s.strip.length > 0)
						infos[importID].push("Gazetteer-ID Text teilweise ignoriert: "+gazetteerIdPlusText+" --> "+$1 )
					end
					statistics[:spatiallyPartOfRegion][i] ||= 0
					statistics[:spatiallyPartOfRegion][i] += 1
				else
					warnings[importID].push("Gazetteer-ID nicht erkannt: "+gazetteerIdPlusText)
				end
			end
			period[:spatiallyPartOfRegion] = gazetteerIDs
		end
	end


# 	if (options[:verbose])
# 		puts "\n"
# 		puts importID
# 		puts addWrapper(period).to_json
# 	end

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


# Spalten, die sich auf andere Einträge beziehen

puts "\n# csv --> json, Teil 2\n" if options[:verbose]

akzeptierteZeilen.each do |row|

	importID = row[columnPos["importID"]]
	period = periods[importID]


	# Spalte: siblings
	# TODO: bisher alles keine Listen; muss man ändern, wenn es Beispiele gibt 

	if (columnPos["siblings"] > -1)
		siblingFeld = row[columnPos["siblings"]]
		isMetInTimeBy = ""
		meetsInTimeWith = ""
		startsAtTheEndOf = ""
		endsAtTheStartOf = ""
	
		if (siblingFeld.to_s.strip.length > 0)
		
			# A0021 (comes before)
			# aat:300107341 (comes before)
			if ( siblingFeld.match(/^ *([a-zA-Z:0-9]+) +\(comes before\) *$/) )
				isMetInTimeBy = $1
				startsAtTheEndOf = $1
			# aat:300107343 (comes after)
			elsif ( siblingFeld.match(/^ *([a-zA-Z:0-9]+) +\(comes after\) *$/) )
				meetsInTimeWith = $1
				endsAtTheStartOf = $1
			# A0077 (comes after) A0089 (comes before)
			elsif ( siblingFeld.match(/^ *([a-zA-Z:0-9]+) +\(comes after\) *([a-zA-Z:0-9]+) +\(comes before\) *$/) )
				isMetInTimeBy = $2
				startsAtTheEndOf = $2
				meetsInTimeWith = $1
				endsAtTheStartOf = $1
			else
				warnings[importID].push("comes before/after wurde nicht erkannt: "+siblingFeld)
			end

			# Allen-Relationen
		
			if (isMetInTimeBy.to_s.strip.length > 0)
				if (concordance2Chron.has_key?(isMetInTimeBy))
					period[:isMetInTimeBy] = concordance2Chron[isMetInTimeBy]
					statistics[:isMetInTimeBy] += 1
				else
					warnings[importID].push("Verweis auf nicht vorhandene ID isMetInTimeBy "+isMetInTimeBy)
				end
			end
			if (meetsInTimeWith.to_s.strip.length > 0)
				if (concordance2Chron.has_key?(meetsInTimeWith))
					period[:meetsInTimeWith] = concordance2Chron[meetsInTimeWith]
					statistics[:meetsInTimeWith] += 1
				else
					warnings[importID].push("Verweis auf nicht vorhandene ID meetsInTimeWith "+meetsInTimeWith)
				end
			end
		
			# fuzzy Relationen
		
			if (startsAtTheEndOf.to_s.strip.length > 0)
				if (concordance2Chron.has_key?(startsAtTheEndOf))
					period[:startsAtTheEndOf] = concordance2Chron[startsAtTheEndOf]
					statistics[:startsAtTheEndOf] += 1
				else
					warnings[importID].push("Verweis auf nicht vorhandene ID startsAtTheEndOf "+startsAtTheEndOf)
				end
			end
			if (endsAtTheStartOf.to_s.strip.length > 0)
				if (concordance2Chron.has_key?(endsAtTheStartOf))
					period[:endsAtTheStartOf] = concordance2Chron[endsAtTheStartOf]
					statistics[:endsAtTheStartOf] += 1
				else
					warnings[importID].push("Verweis auf nicht vorhandene ID endsAtTheStartOf "+endsAtTheStartOf)
				end
			end
		end
	end

	
	# Spalte: parents
	# probehalber werden, abweichend vom Gazetteer, auch die Kinder explizit aufgelistet
	# (nicht so lange Listen wie im Gazetteer, und mehrere Hierarchien)
	# TODO: neuer Name?
	# TODO: auch fallsWithin sollte eine Liste sein, sobald es ein reales Beispiel gibt 
	#       (aber dann muss man auch das Fontend anpassen!)

	if (columnPos["parents"] > -1)
		fallsWithinFeld = row[columnPos["parents"]]
		fallsWithin = ""
		contains = ""

		if (fallsWithinFeld.to_s.strip.length > 0)

			# G0005 --> ist enthalten
			if ( fallsWithinFeld.match(/^ *([a-zA-Z:0-9]+) *$/) )
				fallsWithin = $1
			# A0081 (is part of)
			# aat:300020533 (is part of) --> ist enthalten
			elsif ( fallsWithinFeld.match(/^ *([a-zA-Z:0-9]+) *\(is part of\) *$/) )
				fallsWithin = $1
			# aat:300020666; aat:300020541 (is part of) --> enthält, ist enthalten
			elsif ( fallsWithinFeld.match(/^ *([a-zA-Z:0-9]+) *; *([a-zA-Z:0-9]+) *\(is part of\) *$/) )
				contains = $1
				fallsWithin = $2
			else
				warnings[importID].push("fallsWithin/contains wurde nicht erkannt: "+fallsWithinFeld)
			end

			if (fallsWithin.to_s.strip.length > 0)
				if (concordance2Chron.has_key?(fallsWithin))
					period[:fallsWithin] = concordance2Chron[fallsWithin]
					statistics[:fallsWithin] += 1
				else
					warnings[importID].push("Verweis auf nicht vorhandene ID fallsWithin "+fallsWithin)
				end
			end

			if (contains.to_s.strip.length > 0)
				if (concordance2Chron.has_key?(contains))
					# hier ist klar, dass period[:contains] noch nicht existiert;
					# erst bei den ergänzten Verknüpfungen muss man das prüfen
					period[:contains] = [ concordance2Chron[contains] ]
					statistics[:contains][0] += 1
				else
					warnings[importID].push("Verweis auf nicht vorhandene ID contains "+contains)
				end
			end
		end
	end


	# Spalte: senses

	if (columnPos["senses"] > -1)
		sensesFeld = row[columnPos["senses"]]
		if (sensesFeld.to_s.strip.length > 0)
		
			# A0105, in sense of
			if ( sensesFeld.match(/^ *([a-zA-Z:0-9]+), in sense of *$/) )
				isSenseOf = $1
			else
				warnings[importID].push("sense wurde nicht erkannt: "+sensesFeld)
			end

			if (isSenseOf.to_s.strip.length > 0)
				if (concordance2Chron.has_key?(isSenseOf))
					period[:isSenseOf] = concordance2Chron[isSenseOf]
					statistics[:isSenseOf] += 1
				else
					warnings[importID].push("Verweis auf nicht vorhandene ID isSenseOf "+isSenseOf)
				end
			end
		end
	end

	# Spalte: timeOriginal
	# Spalte: timeStandardized
	# --> der Inhalt von zwei Spalten kommt in dieselbe Struktur!

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
				period[:hasTimespan][0][:source] = source
				statistics[:hasTimespan][0][:source] += 1
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
					# TODO regex lesbarer mache, und nicht-zählende () verwenden
					if ( timeStandardizedFeld.match(/^ *\[(([+-]? ?[0-9]+)|\?)[;,] ?(([+-]? ?[0-9]+)|\?)\] *$/) )
						from = $1
						to = $3
						period[:hasTimespan][0][:from] = from
						period[:hasTimespan][0][:to] = to
						statistics[:hasTimespan][0][:from] += 1
						statistics[:hasTimespan][0][:to] += 1
					else
						warnings[importID].push("timeStandardized wurde nicht erkannt: "+timeStandardizedFeld)
					end
				end
			end
		end
	end

	# Spalte: matching
	# TODO zurzeit noch String, muss aber Liste werden

	if (columnPos["matching"] > -1)
		matchingFeld = row[columnPos["matching"]].to_s.strip
		if (matchingFeld.length > 0)
		
			# sameAs G0008 --> Standardform
			if ( matchingFeld.match(/^ *sameAs +([a-zA-Z:0-9]+) *$/) )
				sameAs = $1
			# G0001, sameAs --> Variation
			elsif ( matchingFeld.match(/^ *([a-zA-Z:0-9]+), sameAs *$/) )
				sameAs = $1
			# G0001, Def/temporal :: sameAs aat:300020058 --> ersten Teil erstmal ignorieren
			elsif ( matchingFeld.match(/ *sameAs +([a-zA-Z:0-9]+) *$/)  )
				sameAs = $1
			else
				warnings[importID].push("matching wurde nicht erkannt: "+matchingFeld)
			end

			if (sameAs.to_s.strip.length > 0)
				if (concordance2Chron.has_key?(sameAs))
					period[:sameAs] = concordance2Chron[sameAs]
					statistics[:sameAs] += 1
				else
					warnings[importID].push("Verweis auf nicht vorhandene ID sameAs "+sameAs)
				end
			end
		end
	end


end


# inverse Properties eintragen

puts "\n# inverse Properties ergänzen\n" if options[:verbose]

akzeptierteZeilen.each do |row|

	importID = row[columnPos["importID"]]
	chronontologyID = concordance2Chron[importID]
	period = periods[importID]

	# vier Fälle: String oder Liste  -->  String oder Liste

	# 1. String --> String
	#    isMetInTimeBy <-> meetsInTimeWith
	#    startsAtTheEndOf <-> endsAtTheStartOf
	#    sameAs --> sameAs (symmetrisch!)
	
	if ( period.has_key?(:isMetInTimeBy) )
		counterpartImportID = concordance2Import[ period[:isMetInTimeBy] ]
		counterpart = periods[ counterpartImportID ]
		if ( counterpart.has_key?(:meetsInTimeWith) )
			if (! counterpart[:meetsInTimeWith].eql?(chronontologyID) )
				warnings[counterpartImportID].push("konnte meetsInTimeWith "+chronontologyID+ "nicht ergänzen, nur einmal erlaubt")
			end
		else
			counterpart[:meetsInTimeWith] = chronontologyID
			infos[counterpartImportID].push("meetsInTimeWith "+chronontologyID+" ergänzt")
			statistics[:meetsInTimeWith] += 1
		end
	end
	if ( period.has_key?(:meetsInTimeWith) )
		counterpartImportID = concordance2Import[ period[:meetsInTimeWith] ]
		counterpart = periods[ counterpartImportID ]
		if ( counterpart.has_key?(:isMetInTimeBy) )
			if (! counterpart[:isMetInTimeBy].eql?(chronontologyID) )
				warnings[counterpartImportID].push("konnte isMetInTimeBy "+chronontologyID+ "nicht ergänzen, nur einmal erlaubt")
			end
		else
			counterpart[:isMetInTimeBy] = chronontologyID
			infos[counterpartImportID].push("isMetInTimeBy "+chronontologyID+" ergänzt")
			statistics[:isMetInTimeBy] += 1
		end
	end
	
	if ( period.has_key?(:startsAtTheEndOf) )
		counterpartImportID = concordance2Import[ period[:startsAtTheEndOf] ]
		counterpart = periods[ counterpartImportID ]
		if ( counterpart.has_key?(:endsAtTheStartOf) )
			if (! counterpart[:endsAtTheStartOf].eql?(chronontologyID) )
				warnings[counterpartImportID].push("konnte endsAtTheStartOf "+chronontologyID+ "nicht ergänzen, nur einmal erlaubt")
			end
		else
			counterpart[:endsAtTheStartOf] = chronontologyID
			infos[counterpartImportID].push("endsAtTheStartOf "+chronontologyID+" ergänzt")
			statistics[:endsAtTheStartOf] += 1
		end
	end	
	if ( period.has_key?(:endsAtTheStartOf) )
		counterpartImportID = concordance2Import[ period[:endsAtTheStartOf] ]
		counterpart = periods[ counterpartImportID ]
		if ( counterpart.has_key?(:startsAtTheEndOf) )
			if (! counterpart[:startsAtTheEndOf].eql?(chronontologyID) )
				warnings[counterpartImportID].push("konnte startsAtTheEndOf "+chronontologyID+ "nicht ergänzen, nur einmal erlaubt")
			end
		else
			counterpart[:startsAtTheEndOf] = chronontologyID
			infos[counterpartImportID].push("startsAtTheEndOf "+chronontologyID+" ergänzt")
			statistics[:startsAtTheEndOf] += 1
		end
	end
	
	if ( period.has_key?(:sameAs) )
		counterpartImportID = concordance2Import[ period[:sameAs] ]
		counterpart = periods[ counterpartImportID ]
		if ( counterpart.has_key?(:sameAs) )
			if (! counterpart[:sameAs].eql?(chronontologyID) )
				warnings[counterpartImportID].push("konnte sameAs "+chronontologyID+ "nicht ergänzen, nur einmal erlaubt")
			end
		else
			counterpart[:sameAs] = chronontologyID
			infos[counterpartImportID].push("sameAs "+chronontologyID+" ergänzt")
			statistics[:sameAs] += 1
		end
	end
	
	# 2. String --> Liste
	#    fallsWithin --> contains
	#    isSenseOf --> hasSense

	if ( period.has_key?(:fallsWithin) )
		counterpartImportID = concordance2Import[ period[:fallsWithin] ]
		counterpart = periods[ counterpartImportID ]

		if ( counterpart.has_key?(:contains) )
			if (! counterpart[:contains].include?( chronontologyID ) )
				counterpart[:contains].push(chronontologyID)
				infos[counterpartImportID].push("contains "+chronontologyID+" ergänzt")

				i = counterpart[:contains].length - 1
				statistics[:contains][i] ||= 0
				statistics[:contains][i] += 1
			end
		else
			counterpart[:contains] = [ chronontologyID ]
			infos[counterpartImportID].push("contains "+chronontologyID+" ergänzt")
			statistics[:contains][0] += 1
		end
	end

	if ( period.has_key?(:isSenseOf) )
		counterpartImportID = concordance2Import[ period[:isSenseOf] ]
		counterpart = periods[ counterpartImportID ]

		if ( counterpart.has_key?(:hasSense) )
			if (! counterpart[:hasSense].include?( chronontologyID ) )
				counterpart[:hasSense].push(chronontologyID)
				infos[counterpartImportID].push("hasSense "+chronontologyID+" ergänzt")

				i = counterpart[:hasSense].length - 1
				statistics[:hasSense][i] ||= 0
				statistics[:hasSense][i] += 1
			end
		else
			counterpart[:hasSense] = [ chronontologyID ]
			infos[counterpartImportID].push("hasSense "+chronontologyID+" ergänzt")
			statistics[:hasSense][0] += 1
		end
	end

	# 3. Liste --> String
	#    contains --> fallsWithin
	
	if ( period.has_key?(:contains) )
		period[:contains].each do |contains|
			counterpartImportID = concordance2Import[ contains ]
			counterpart = periods[ counterpartImportID ]
			if ( counterpart.has_key?(:fallsWithin) )
				if (! counterpart[:fallsWithin].eql?(chronontologyID) )
					warnings[counterpartImportID].push("konnte fallsWithin "+chronontologyID+ "nicht ergänzen, nur einmal erlaubt")
				end
			else
				counterpart[:fallsWithin] = chronontologyID
				infos[counterpartImportID].push("fallsWithin "+chronontologyID+" ergänzt")
				statistics[:fallsWithin] += 1
			end
		end
	end

	# 4. Liste --> Liste
	#    ...

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

