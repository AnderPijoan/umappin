#!/bin/sh
export http_proxy=http://proxy-s-priv.deusto.es:3128/
cd /usr/share/umappin
unset $(git rev-parse --local-env-vars)
git pull origin master
pidof java |xargs kill 
play clean compile stage
cp -r target/scala-2.10/resource_managed/main/public/. /var/www/umappin/
cp -r public/. /var/www/umappin/
for x in 000 100; do 
   target/start -Dpidfile.path=p$x -Dhttp.port=9$x&	
done
target/start -Dpidfile.path=/dev/null -Dhttp.port=8001&
