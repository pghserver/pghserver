<div align="center">
	<h1><img alt="PghServer" src=".github/branding/PghServer.svg" width="256" /></h1>
</div>

> Refer to the [API README](api/README.md) for help with creating a plugin, and including the necessary libraries for
> development.

**PghServer** (Production-Grade HTTP Server) is an actively-maintained HTTP server with a built-in plugin system.

It's designed for users and businesses who want to dedicate to one web server software throughout the site's timeline,
without much hassle when maybe you want a forum now, or maybe you want to generate a wiki from markdown files, or maybe
you just want to serve raw HTML.

It does not ship with any pages or functionality by default, but a note below recommends a plugin that lets you browse
files on your site.

> **Note:** We recommend new sites download
> the [official static file server plugin](https://github.com/pghserver/pghstatic) JAR and drag it into the `plugins/`
> directory. A full description and details are also available on that page.