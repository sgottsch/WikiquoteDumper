#!/bin/bash

reverse() {
  tac <(echo "$@" | tr ' ' '\n') | tr '\n' ' '
}

languages="it en pl ru cs fa de et pt fr uk es he sk tr bs ca eo fi az sl lt zh ar bg hy hr el su nn id sv li hu ko nl ja la ta sah sr gu gl ur te be cy no ml sq vi kn ro eu ku uz hi th ka da sa is"

path = "/home/...../"

mkdir jsons
mkdir logs

for language in `reverse $languages`; do
    echo ${language}
    nohup java -Xmx8G -jar WikiquoteDumper.jar ${language} ${path}/data ${path}/jsons 16 > logs/nohup_${language}.out
done

