#!/usr/bin/env bash

yes | rm -rf generated/*

sbt run

yes | rm -rf $WORKSPACE/integration-catalogue-frontend/app/assets/apiplatformspecs/* 

cp generated/* $WORKSPACE/integration-catalogue-frontend/app/assets/apiplatformspecs 
