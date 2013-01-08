#!/bin/sh

JAVABIN=`which java`

if [ ! -x "${JAVABIN}" ]; then
	echo "You need J2RE to run this program!"
	echo "Aborting..."
	exit 1
fi
cd class
java Run

