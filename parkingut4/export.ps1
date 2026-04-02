# Define el nombre del archivo de salida
$outputFile = "proyecto.txt"

# Vacía (o crea) el archivo de salida
Set-Content -Path $outputFile -Value ""

# Función auxiliar para agregar encabezado y contenido de un archivo
function Append-FileWithHeader {
    param([string]$FilePath)
    $relativePath = Resolve-Path -Path $FilePath -Relative
    Add-Content -Path $outputFile -Value "===== $relativePath ====="
    Get-Content -Path $FilePath | Add-Content -Path $outputFile
    Add-Content -Path $outputFile -Value ""
}

# 1. Agregar todos los archivos .kt recursivamente desde el directorio actual
Get-ChildItem -Path . -Recurse -Include *.java | ForEach-Object {
    Append-FileWithHeader -FilePath $_.FullName
}

# 2. Agregar app/build.gradle.kts si existe
#$gradleFile = "app/build.gradle.kts"
#if (Test-Path $gradleFile) {
#   Append-FileWithHeader -FilePath $gradleFile
#}

# 3. Agregar gradle/libs.versions.toml si existe
#$versionsFile = "gradle/libs.versions.toml"
#if (Test-Path $versionsFile) {
#    Append-FileWithHeader -FilePath $versionsFile
#}