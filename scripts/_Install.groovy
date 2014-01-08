ant.mkdir(dir:"${basedir}/test/phantomjs")
ant.mkdir(dir:"${basedir}/web-app/js/domain")
ant.mkdir(dir:"${basedir}/web-app/js/remoteDomain")
new File("${basedir}/web-app/js/domain.js").text = ''
new File("${basedir}/web-app/js/remoteDomain.js").text = ''
