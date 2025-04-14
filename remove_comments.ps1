# PowerShell script to remove single-line comments from Java test files
$testDir = "src/test"
$javaFiles = Get-ChildItem -Path $testDir -Filter "*.java" -Recurse

foreach ($file in $javaFiles) {
    Write-Host "Processing $($file.FullName)"
    
    # Read the file content
    $content = Get-Content -Path $file.FullName
    
    # Remove single-line comments (lines that only contain a comment)
    $newContent = $content | ForEach-Object {
        if ($_ -match "^\s*//") {
            # Skip lines that are just comments
            return $null
        } elseif ($_ -match "(.*?)//(.*)") {
            # For lines with inline comments, remove the comment part
            return $matches[1].TrimEnd()
        } else {
            # Return lines without comments unchanged
            return $_
        }
    }
    
    # Write the modified content back to the file
    Set-Content -Path $file.FullName -Value $newContent
}

Write-Host "Comment removal completed." 