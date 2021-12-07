#!/bin/bash

# All the languages with at least 50 pages from https://meta.wikimedia.org/wiki/Wikiquote (see also https://wikistats.wmcloud.org/display.php?t=wq), minus simple English
languages="it en pl ru cs fa de et pt fr uk es he sk tr bs ca eo fi az sl lt zh ar bg hy hr el su nn id sv li hu ko nl ja la ta sah sr gu gl ur te be cy no ml sq vi kn ro eu ku uz hi th ka da sa is"

date="20211001"
datedbpedia1="2021.03.01"
datedbpedia2="2021.03.01"

echo "DATE: $date"

mkdir data
cd data

wget --no-check-certificate -O "ids.ttl.bz2" "https://downloads.dbpedia.org/repo/dbpedia/wikidata/sameas-all-wikis/${datedbpedia2}/sameas-all-wikis.ttl.bz2"
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
    wget --no-check-certificate -O "${language}/wikidata.sql.gz" "${url}wbc_entity_usage.sql.gz"
    gunzip "${language}/wikidata.sql.gz"
    
    wget --no-check-certificate -O "${language}/pages.xml.bz2" "${url}pages-meta-current.xml.bz2"
    bzip2 -d "${language}/pages.xml.bz2"
    
	if [ "$language" = "en" ]; then
			cp ids_en.ttl en/ids.ttl
		else
			grep " ${language} " ids.ttl > "${language}/ids.ttl"
			sed -i "s/ ${language} / /g" "${language}/ids.ttl"		
	fi;

    grep -A 2 '<title>' data/${language}/pages.xml | grep -v '<ns>' | tr --delete '\n'  > data/${language}/wikiquote_titles.tsv
    sed -i 's/<\/title>    <id>/\t/g;s/<\/id>--    <title>/\n/g;s/<\/id>//g;s/    <title>//g' data/${language}/wikiquote_titles.tsv
done

wget --no-check-certificate -O "types.ttl.bz2" "https://downloads.dbpedia.org/repo/dbpedia/wikidata/instance-types/${datedbpedia1}/instance-types_specific.ttl.bz2"
bzip2 -d "types.ttl.bz2"
sed -i 's/<http:\/\/wikidata.dbpedia.org\/resource\/Q//g ;s/> <http:\/\/www.w3.org\/1999\/02\/22-rdf-syntax-ns#type> <http:\/\/dbpedia.org\/ontology\// /g ; s/> .//g' "types.ttl"

grep 'Person' types.ttl | cut -d ' ' -f1 > persons.csv

echo "Done."