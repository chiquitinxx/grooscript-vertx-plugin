grooscript-vertx-plugin 0.3-SNAPSHOT
====================================

Grails plugin to help develop with GrooScript and automatically reloads the page with vert.x. You can activate only the GrooScript conversion, only the vert.x server, or both.

**Use this plugin only in development. If you change any config option, you must restart the server.**
**You need java 1.7 to use this plugin, vert.x requires java 1.7

If you need more information about GrooScript visit [grooscript.org](http://grooscript.org), the pluging internals just launch the grooscript conversion daemon, that detects modification in your .groovy files, and convert to javascript.

[Vert.x](http://vertx.io) is needed for reload pages when changes are done in files. For example you can see changes in *.css, *.js, *.gsp, … automatically in your browser without reload the page. That speed up your development time, no logic added, any observed file that you change, will send the reload event in your browsers.

If vert.x and grooscript are up, converted files reload page too.

It uses resources plugin also, and some gpars dataflows.

How do I setup all this things?

1.- Install the plugin. In your Build.config

compile ":grooscript-vertx:0.3"

2.- Set Groovy files to watch to be converted with the GrooScript daemon. (optional)

    //Each second detects changes in source groovy files and generate js files.
    grooscript {
        //An array of files and/or folders
        source = ['src/groovy/Me.groovy']
        //A folder where .js files will be saved
        destination = 'web-app/js'
    }

3.- If you are using grooscript conversion, that converted .js files, need grooscript.js to work.

You can do this with resources plugin, in your Config.groovy:

    grails.resources.modules = {
        myModule {
            dependsOn('grooscript') // grooscript available from plugin
            resource url:'/js/MyFile.js'
            ..
        }
        otherModule { ....
    }

And then use in your gsp:

    <r:require module="myModule"/>

4.- If you want to use vert.x for automatically reload pages if changes detected (optional). Set the vert.x port in Config.groovy to launch it

    vertx {
        eventBus {
            //port to run vert.x
            port = 8085
            //Host is optional, by default is localhost
            //host = 'localhost'
        }
        //Set directories / files to watch and send reload signal to browsers.
        //Listener runs each second
        listener {
            //List of files or folders to watch
            source = ['web-app/css']
            //You can execute a closure (must be called afterChanges) that recieve the list of files changed(absolute path text)
            afterChanges = { list ->
                list.each { fileName ->
                    println '** file changed '+fileName
                }
            }
        }
    }

5.- Finally, in the GSP pages, if you want that page automatically reloads, need listen to reload event from vert.x, so must add this tag:

	<grooscript:reloadPage/>

## Example

For example, I have used this .gsp for my tests:

	<%@ page contentType="text/html;charset=UTF-8" %>
	<html>
	<head>
    	<r:require module="me"/>
  		<title>Title!</title>
    	<r:require module="style"/>
    </head>
	<body>
		<p>Hello!</p>
		<grooscript:reloadPage/>
	</body>
	</html>

And in the Config.groovy file:

    vertx {
        eventBus {
            port = 8085
            //host = 'localhost'
        }
        listener {
            source = ['web-app/css']
            afterChanges = { list ->
                list.each {
                    println 'CHANGED!!! '+it
                }
            }
        }
    }

	grooscript {
    	source = ['scripts/Me.groovy']
    	destination = 'web-app/js'
	}

	grails.resources.modules = {
    	style {
        	resource url:'/css/style.css'
    	}
    	me {
        	dependsOn('grooscript')
        	resource url:'/js/Me.js'
    	}
	}

Then, if I modify any file in the web-app/css, page reloads. And same for scripts/Me.groovy file, if I modify that file, then will be converted to web-app/js/Me.js and page will refresh automatically.


