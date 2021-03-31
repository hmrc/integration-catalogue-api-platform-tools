#!/usr/bin/env bash

yes | rm -rf generated/*

sbt run
