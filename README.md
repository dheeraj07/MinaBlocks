# MinaBlocks
<b>
Finds the total number of missing blocks across all the servers

Steps to run:
1. Run the side car in the same node where the Mina daemon is running and create a ".log" file using the output from Mina side car
2. In your local system, create a yml file with the name "serverAddresses.yml" and insert all your server/node ip-addresses and passwords. Make sure each credential is seperated by a line break.<br>
  Example: "ip-address": "password"
3. Find "filePaths.yml" file and replace the server path and local path.
  "server" path: The place where ".log" files are created in your Mina node.
  "local" path: The place where the missing blocks difference output gets generated<b>
