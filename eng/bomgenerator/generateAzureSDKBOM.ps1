$repoRoot = Resolve-Path "${PSScriptRoot}..\..\.."
$inputDir = Join-Path ${PSScriptRoot} "inputDir"
$outputDir = Join-Path ${PSScriptRoot} "outputDir"
$versionClientFileName = "version_client.txt"
$pomFileName = "pom.xml"
$defaultVersionClientFilePath = Join-Path $inputDir $versionClientFileName
$defaultPomFilePath = Join-Path $inputDir $pomFileName
$versionClientFilePath = Join-Path $repoRoot "eng" "versioning" $versionClientFileName
$bomPomFilePath = Join-Path $repoRoot "sdk" "boms" "azure-sdk-bom" $pomFileName

if(! (Test-Path $inputDir)) { 
  New-Item -Path $PSScriptRoot -Name "inputDir" -ItemType "directory"
}

if(! (Test-Path $defaultVersionClientFilePath)) {
 Copy-Item $versionClientFilePath -Destination $inputDir
}

if(! (Test-Path $defaultPomFilePath)) {
 Copy-Item $bomPomFilePath -Destination $inputDir
}

echo "Run the following to generate the Pom file and dependency closure report. Both files will be generated in the outputDir."
echo "mvn exec:java -Dexec.args=`"-inputDir=$inputDir -outputDir=$outputDir -mode=generate`""