# WikiquoteDumper
The WikiquoteDumper downloads Wikiquote dumps in any languages and converts them into JSON format.

## Extraction pipeline

First, download and pre-process the dump files from Wikiquote using the following script in the "scripts" folder:
- dumps_download.sh
Before running, update the "languages" and "date" parameters as required, in correspondence to the Wikiquote and DBpedia dumps.

Then, run the WikiquoteDumper with the following command.
- dumps_processing.sh
Before running, update the "path" parameter in the script.