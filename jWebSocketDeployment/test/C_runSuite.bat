@echo off

echo ----------------------------------------------------------------------------
echo WAIT FOR SERVER START UP TO START TEST SUITE IN %1....
echo ----------------------------------------------------------------------------
pause

start "Running test suite in %1-Browser..." %2 %3

echo ----------------------------------------------------------------------------
echo Please run the tests now in %1 and press any key when tests are done...
echo ----------------------------------------------------------------------------
pause


