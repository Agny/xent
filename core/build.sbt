excludeFilter in Compile := new SimpleFileFilter(file => file.getPath.endsWith(".json"))
excludeFilter in Test := HiddenFileFilter
test in assembly := {}