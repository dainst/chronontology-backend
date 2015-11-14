#!/usr/bin/ruby
# encoding: utf-8

require 'dbi'
require 'rest-client'
require 'json'
require "unicode_utils"

ops = eval(File.open('config.rb') {|f| f.read })
db_user = ops[:db_user]
db_password = ops[:db_password]
api_user = ops[:api_user]
api_password = ops[:api_password]

db = DBI.connect("DBI:Mysql:arachne:arachne.uni-koeln.de", db_user, db_password)
db.execute("SET NAMES 'utf8'")

api = RestClient::Resource.new("0.0.0.0:4567", :user => api_user, :password => api_password)

sql = "SELECT Inhalt FROM `wertelisten` WHERE  `FS_WlID` = 88"
result = db.execute(sql)
last_id = ""
last_parent = []
last_sibling = ""
last_level = 1
last_group = []
last_label = ""
periods = {}

i=1
result.fetch do |row|
	
	label = row[0].force_encoding("UTF-8").strip.downcase
	next if label.empty?

	# normalize some labels
	label = label
		.sub("sm iiia1", "sm iii a 1")
		.sub("sm iiia2", "sm iii a 2")
		.sub("sh iiia1", "sh iii a 1")
		.sub("sm iiib", "sm iii b")

	if label.start_with? '-' 
		level = 1
	elsif last_level == 1
		level = last_level + 1
	elsif label.end_with? last_label
		level = last_level + 1
		last_group << last_label
	elsif label.end_with? ' 1', ' i', ' a', ' früh'
		level = last_level + 1
		last_group << label.sub(/ \p{Word}+$/, '')
	elsif last_group.size > 0
		level = last_level
		while last_group.size > 0 do
			break if label.start_with?(last_group.last + ' ') || label.end_with?(last_group.last)
			last_group.pop
			level -= 1
		end
	else
		level = last_level
	end

	if level > last_level
		last_parent << last_id
		last_sibling = ""
	elsif level == last_level
		last_sibling = last_id
	else
		while last_level > level do
			last_sibling = last_parent.pop
			last_level -= 1
		end
	end

	label = label.tr('-','').strip
	label = UnicodeUtils.titlecase label
	label = label.sub('Ii', 'II').sub('IIi', 'III')
	
	period = {
		"prefLabel" => {
			"de" => label
		},
		"provenance" => ["Arachne"]
	}

	puts "%-50s %-50s %-50s" % [(" " * level) + label, "parent: #{last_parent.last}", "last_sibling: #{last_sibling}"]

	if last_parent.size > 0
		period["fallsWithin"] = periods[last_parent.last]["@id"]
	end

	if !last_sibling.empty?
		period["isMetInTimeBy"] = periods[last_sibling]["@id"]
	end

	puts period.to_json
	response = api["period/"+i.to_s].put(period.to_json, :content_type => :json, :accept => :json)
    last_id = response.headers[:location]
    last_label = label.downcase
    periods[last_label] = JSON.parse(response.body)

	# also add reverse property to sibling
    if !last_sibling.empty?
    	sibling = periods[last_sibling]
		sibling["meetsInTimeWith"] = periods[last_label]["@id"]
		puts "sibling" + sibling["@id"].to_s
		api[sibling["@id"]].put(sibling.to_json, :content_type => :json, :accept => :json)
	end

    last_level = level
    last_id = "#{last_label}"

	i=i+1
end
