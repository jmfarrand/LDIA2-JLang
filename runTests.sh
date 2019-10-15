#!/bin/sh
echo "=== Test01.jlang ==="
java -classpath ./bin Jlang < 'jlang-testscripts/test01.jlang'
echo "=== Test02.jlang ==="
java -classpath ./bin Jlang < 'jlang-testscripts/test02.jlang'
echo "=== Test03.jlang ==="
java -classpath ./bin Jlang < 'jlang-testscripts/test03.jlang'
echo "=== Test04.jlang ==="
java -classpath ./bin Jlang < 'jlang-testscripts/test04.jlang'
echo "=== Test05.jlang ==="
java -classpath ./bin Jlang < 'jlang-testscripts/test05.jlang'

