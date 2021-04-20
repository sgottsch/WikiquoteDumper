#!/bin/bash

# All the languages with at least 100 pages from https://wikistats.wmcloud.org/display.php?t=wq / https://meta.wikimedia.org/wiki/Wikiquote
languages="en it pl ru cs fa de pt es fr uk he sk bs tr ca fi az sl lt eo zh et bg ar hr hy el su nn id sv li hu ko nl ja la ta sah sr simple gu gl th ur te be cy no ml sq kn ro ku eu uz hi ka da vi sa is"

date="20210320"
datedbpedia2="2021.03.01"
datedbpediaids="2021.03.01"

echo "DATE: $date"

mkdir data
cd data

wget -O "ids.ttl.bz2" "https://downloads.dbpedia.org/repo/dbpedia/wikidata/sameas-all-wikis/${datedbpediaids}/sameas-all-wikis.ttl.bz2"
bzip2 -d ids.ttl.bz2

grep '.dbpedia.org/resource/' ids.ttl > ids_filtered.ttl
grep 'http://dbpedia.org/resource/' ids.ttl > ids_en.ttl

rm ids.ttl
mv ids_filtered.ttl ids.ttl

sed -i 's/<http:\/\/wikidata.dbpedia.org\/resource\/Q//g ; s/> <http:\/\/www.w3.org\/2002\/07\/owl#sameAs> <http:\/\// /g ; s/.dbpedia.org\/resource\// /g ; s/> .//g' ids.ttl
sed -i 's/<http:\/\/wikidata.dbpedia.org\/resource\/Q//g ; s/> <http:\/\/www.w3.org\/2002\/07\/owl#sameAs> <http:\/\/dbpedia.org\/resource\// /g ; s/> .//g' ids_en.ttl

for language in $languages; do

	echo ${language}

    url="https://dumps.wikimedia.org/${language}wikiquote/${date}/${language}wikiquote-${date}-"

    mkdir ${language}
    wget -O "${language}/wikidata.sql.gz" "${url}wbc_entity_usage.sql.gz"
    gunzip "${language}/wikidata.sql.gz"
    
    wget -O "${language}/pages.xml.bz2" "${url}pages-meta-current.xml.bz2"
    bzip2 -d "${language}/pages.xml.bz2"
    
	if [ "$language" = "en" ]; then
			cp ids_en.ttl en/ids.ttl
		else
			grep " ${language} " ids.ttl > "${language}/ids.ttl"
			sed -i "s/ ${language} / /g" "${language}/ids.ttl"		
	fi;
   
done

wget -O "types.ttl.bz2" "https://downloads.dbpedia.org/repo/dbpedia/wikidata/instance-types/${datedbpedia2}/instance-types_specific.ttl.bz2"
bzip2 -d "types.ttl.bz2"
sed -i 's/<http:\/\/wikidata.dbpedia.org\/resource\/Q//g ;s/> <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#type> <http:\/\/dbpedia.org\/ontology\// /g ; s/> .//g' "types.ttl"

grep 'Person' types.ttl | cut -d ' ' -f1 > persons.csv

echo "Done."

