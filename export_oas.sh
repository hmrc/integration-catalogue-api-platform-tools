#!/usr/bin/env bash

yes | rm -rf generated/*
# yes | rm -rf $WORKSPACE/integration-catalogue-oas-files/platforms/api-platform/* 

sbt 'run --generateOas --reviewedDate '$1

# cp generated/* $WORKSPACE/integration-catalogue-oas-files/platforms/api-platform/
