'use strict';

import * as fs from "fs"
import * as path from 'path';
import * as net from 'net';
import * as child_process from "child_process";
import * as vscode from 'vscode';

import { workspace, Disposable, ExtensionContext } from 'vscode';
import { LanguageClient, LanguageClientOptions, SettingMonitor, StreamInfo } from 'vscode-languageclient';

export function activate(context: ExtensionContext) {
	console.log("starting spoon")
	function createServer(): Promise<StreamInfo> {
		return new Promise((resolve, reject) => {
			var server = net.createServer((socket) => {
				console.log("Creating server");

				resolve({
					reader: socket,
					writer: socket
				});

				socket.on('end', () => console.log("Disconnected"));
			}).on('error', (err) => {
				// handle errors here
				throw err;
			});

			let javaExecutablePath = findJavaExecutable('java');

			// grab a random port.
			server.listen(6009, () => {
				// Start the child java process
				let options = { cwd: workspace.rootPath };

				let args = ["-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=127.0.0.1:8000",
					'-jar',
					// path.resolve(context.extensionPath, '..', 'build', 'libs', 'spoon-langserver.jar'),
					path.resolve(context.extensionPath, 'spoon-langserver.jar'),
					"6009"
				]

				let process = child_process.spawn(javaExecutablePath, args, options);

				// Send raw output to a file
				if (!fs.existsSync(context.storagePath))
					fs.mkdirSync(context.storagePath);

				let logFile = context.storagePath + '/vscode-languageserver-java-example.log';
				let logStream = fs.createWriteStream(logFile, { flags: 'w' });

				process.stdout.pipe(logStream);
				process.stderr.pipe(logStream);

				console.log(`Storing log in '${logFile}'`);
			});
		});
	};

	// Options to control the language client
	let clientOptions: LanguageClientOptions = {
		// Register the server for plain text documents
		documentSelector: ['java'],
		synchronize: {
			// Synchronize the setting section 'languageServerExample' to the server
			configurationSection: 'languageServerExample',
			// Notify the server about file changes to '.clientrc files contain in the workspace
			fileEvents: [
				workspace.createFileSystemWatcher('**/.clientrc'),
				workspace.createFileSystemWatcher('*.java')]
		}
	}

	// Create the language client and start the client.
	let disposable = new LanguageClient('languageServerExample', 'Language Server Example', createServer, clientOptions).start();

	// Push the disposable to the context's subscriptions so that the 
	// client can be deactivated on extension deactivation
	console.log("spoon started")
	context.subscriptions.push(disposable);

}

// MIT Licensed code from: https://github.com/georgewfraser/vscode-javac
function findJavaExecutable(binname: string) {
	binname = correctBinname(binname);

	// First search each JAVA_HOME bin folder
	if (process.env['JAVA_HOME']) {
		let workspaces = process.env['JAVA_HOME'].split(path.delimiter);
		for (let i = 0; i < workspaces.length; i++) {
			let binpath = path.join(workspaces[i], 'bin', binname);
			if (fs.existsSync(binpath)) {
				return binpath;
			}
		}
	}

	// Then search PATH parts
	if (process.env['PATH']) {
		let pathparts = process.env['PATH'].split(path.delimiter);
		for (let i = 0; i < pathparts.length; i++) {
			let binpath = path.join(pathparts[i], binname);
			if (fs.existsSync(binpath)) {
				return binpath;
			}
		}
	}

	// Else return the binary name directly (this will likely always fail downstream) 
	return null;
}

function correctBinname(binname: string) {
	if (process.platform === 'win32')
		return binname + '.exe';
	else
		return binname;
}