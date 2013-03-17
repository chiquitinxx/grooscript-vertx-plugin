grooscript-vertx-plugin
=======================

Grails plugin to help develop with GrooScript and automatically reloads the page with vert.x. You can activate only the GrooScript conversion, only the vert.x server, or both.

**Still plugin in development. I have added plugin to a project and it works. I don't have many Grails experience, any suggestion or comment are welcome. If you change any config option, you must restart the server.**

If you need more information about GrooScript visit [grooscript.org](http://grooscript.org), the pluging internals just launch the grooscript conversion daemon, that detects modification in your .groovy files, and convert to javascript.

[Vert.x](http://vertx.io) is needed for reload pages when changes are done in files. For example you can see changes in *.css, *.js, *.gsp, … automatically in your browser without reload the page. That speed up your development time, no logic added, any observed file that you change, will send the reload event in your browsers.

If vert.x and grooscript are up, converted files reload page too.

How do I setup all this things?

1.- Install the plugin. The zip is here, while I finish the development and upload to grails plugins repository.

2.- Add dependencies in BuildConfig.groovy

    dependencies {
        runtime 'org.vert-x:vertx-lang-groovy:1.3.1.final'
        runtime 'org.grooscript:grooscript:0.2.3'
    }

3.- Set Groovy files to watch to be converted with the GrooScript daemon. (optional)

    grooscript {
        //An array of files and folders
        source = ['src/groovy/Me.groovy']
        //A folder where .js files will be saved
        destination = 'web-app/js'
    }

4.- If you are using grooscript conversion, that converted .js files, need grooscript.js to work.

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

5.- If you want to use vert.x for automatically reload pages if changes detected (optional). Set the vert.x port in Config.groovy to launch it

    vertx {
        eventBus {
            port = 8085
            //Host is optional, by default is localhost
            //host = 'localhost'
        }
    }

6.- Set directories / files to watch and send reload signal to browsers in Config.groovy. (need vertx running)

    //A list of files and/or folders
    savedFiles.listener = ['web-app/css']

7.- Finally, in the GSP pages, if you want that page automatillay reloads, need listen to reload event from vert.x, so must add this tag:

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
    	}
	}

	grooscript {
    	source = ['scripts/Me.groovy']
    	destination = 'web-app/js'
	}

	savedFiles.listener = ['web-app/css']

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


