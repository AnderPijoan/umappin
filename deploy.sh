#!/bin/sh
export http_proxy=http://proxy-s-priv.deusto.es:3128/
cd /usr/share/umappin
unset $(git rev-parse --local-env-vars)
git pull origin master
pidof java |xargs kill 
play clean compile stage
for x in 000 100;
 do 
   target/start -Dpidfile.path=p$x -Dhttp.port=9$x&	
done
