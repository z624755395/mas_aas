# mas_aas

This project implements product agents and resource agents.

## Table of Contents

- [Background](#background)
- [Install](#install)
- [Usage](#usage)
- [Jade Operation](#jade)


## Background
In this project, Eclipse Basyx SDK and JADE are used.  
The documentation of Eclipse Basyx: https://wiki.eclipse.org/BaSyx
Agents are implemented using Jade(JAVA Agent DEvelopment Framework)
The website of Jade:https://jade.tilab.com/

## Install
1. Import projects  
2. maven clean install

## Usage 
Multi-Agent System:
1. with eclipse:  
->run as java application;  
->Main class choose jade.Boot;  
->Arguments:-gui ProductAgent:multiAgentSystem.ProductAgent;ResourceAgent1:multiAgentSystem.ResourceAgent1;ResourceAgent2:multiAgentSystem.ResourceAgent2
2. Or use commandline:  
->add classpath: set CLASSPATH=%CLASSPATH%;.;(systempath)\lib\jade.jar;(systempath)\lib\commons-codec-1.3.jar;(systempath)\lib\multiAgentSystem-1.1.0.jar  
->java jade.Boot -gui ProductAgent:multiAgentSystem.ProductAgent;ResourceAgent1:multiAgentSystem.ResourceAgent1;ResourceAgent2:multiAgentSystem.ResourceAgent2

## jade
after jade successfully started, click Main-Container -> Actions -> start new agent with:  
class name: multiAgentSystem.ProductAgent  
arguments: process.p1
