#!/usr/bin/env bash

yes | rm -rf generated/*

yes | rm -rf $WORKSPACE/integration-catalogue-frontend/app/assets/apiplatformspecs/* 

sbt run

cp generated/* $WORKSPACE/integration-catalogue-frontend/app/assets/apiplatformspecs 
