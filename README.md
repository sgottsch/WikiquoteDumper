# WikiquoteDumper
The WikiquoteDumper downloads Wikiquote dumps in any languages and converts them into JSON format.

## Extraction pipeline

First, download and pre-process the dump files from Wikiquote using the following script in the `scripts` folder:
- `sh dumps_download.sh` (before running, update the "languages" and "date" parameters as required, in correspondence to the Wikiquote and DBpedia dumps)

Export the WikiquoteDumper class as an executable jar file called WikiquoteDumper.jar.

Then, run the WikiquoteDumper with the following command.
- `sh dumps_processing.sh` (before running, update the "path" parameter in the script)

## Wikiquote to Wikidata mapping extraction.

To create a file of mappings between Wikiquote IDs to Wikidata IDs, run (if not done already) the `dumps_download.sh` script as explained above and run `de.l3s.cleopatra.quotekg.data.WikiquoteToWikidataMapCreator` with two parameters: the data folder and the name of the output file.