# SpoonLanguageServer
First example implementation for a Spoon language server.

# What is a Language Server

This repo is a first draft for a language server implementation for spoon.
A language server can provide different feature to a client, like completion, syntax highlighting and code actions.
Several language server client exist like vim, visual studio code, eclipse theia... etc.
The communication between server and client is specified here [spec](https://microsoft.github.io/language-server-protocol/).
The eclipse foundation provides an implementation of the protocol [here](https://github.com/eclipse/lsp4j).

# Why? Want is the goal of this?

Using this protocol we can connect spoon to an IDE and provide features like refactoring.
See ExampleCodeAction for an example. The benefit of this is, that a user can use multiple language server for the same program language.
So we don't have to provide already solved things like syntax highlighting, because a user can connect the same client to [redhat's](https://github.com/redhat-developer/vscode-java) and spoons language server.

# who can I test it?

## with vscode

1. Build the source with *gradle build*.
2. *cd client* and *npm install*
3. open ./client in vscode and use the provided launch.json 
4. You can connect java debugger on port 8000 

## with different language client

Currently the server tries to connect to port 6009. In Start.java you can change the port. Let your client listen on that port. 

# What is currently provided?

1. More less gimmick hover, showing the ast element your mouse currently hovers.
2. Example refactoring add a new comment line over the current element
