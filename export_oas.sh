#!/usr/bin/env bash

yes | rm -rf generated/*
yes | rm -rf $WORKSPACE/integration-catalogue-oas-files/platforms/api-platform/* 

sbt run

cp generated/* $WORKSPACE/integration-catalogue-oas-files/platforms/api-platform/
