#languages="en it pl ru cs fa de pt es fr uk he sk bs tr ca fi az sl lt eo zh et bg ar hr hy el su nn id sv li hu ko nl ja la ta sah sr simple gu gl th ur te be cy no ml sq kn ro ku eu uz hi ka da vi sa is"
languages="is sa vi da ka hi uz eu ku ro kn sq ml no cy be te ur th gl gu simple sr sah ta la ja nl ko hu li sv id nn su el hy hr ar bg et zh eo lt sl az fi ca tr bs sk he uk fr es pt de fa cs ru pl it en"

path = "/home/...../"

mkdir jsons
mkdir logs

for language in $languages; do
    echo ${language}
    nohup java -Xmx8G -jar WikiquoteDumper.jar ${language} ${path}/data ${path}/jsons 16 > logs/nohup_${language}.out
done

