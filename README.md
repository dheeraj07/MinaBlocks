# MinaBlocks
<b>
Finds the total number of missing blocks across all the servers

Steps to run:
1. Run the side car in the same node where the Mina daemon is running and create a ".log" file using the output from Mina side car
2. Create 3 files in your local machine where you're planning to run this script:<br>
  -> serverAddresses.yml: [Keyvalue pair format]=> "ipaddress of the server" : "password of the server"<br>
  -> serverUserNames.yml: [Keyvalue pair format]=> "ipaddress of the server" : "username of the server"<br>
  -> filePaths.yml: [Keyvalue pair format] => <br>
        &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; a. "server" : Mention the file path of the log file in the remote server. <br>&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; Example: "/root" (Make sure you are creating the log files with the extension ".log")<br>
       &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; b. "local" : Mention the local path where you wanted to store the output. <br>&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; Example: "/Users/dheeraj/Documents/Mina-Logs/"
