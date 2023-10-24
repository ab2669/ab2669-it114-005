<table><tr><td> <em>Assignment: </em> It114 Milestone1</td></tr>
<tr><td> <em>Student: </em> Andrew Boksz (ab2669)</td></tr>
<tr><td> <em>Generated: </em> 10/23/2023 10:50:22 PM</td></tr>
<tr><td> <em>Grading Link: </em> <a rel="noreferrer noopener" href="https://learn.ethereallab.app/homework/IT114-005-F23/it114-milestone1/grade/ab2669" target="_blank">Grading</a></td></tr></table>
<table><tr><td> <em>Instructions: </em> <ol><li>Create a new branch called Milestone1</li><li>At the root of your repository create a folder called Project if one doesn't exist yet</li><ol><li>You will be updating this folder with new code as you do milestones</li><li>You won't be creating separate folders for milestones; milestones are just branches</li></ol><li>Create a milestone1.md file inside the Project folder</li><li>Git add/commit/push it to Github (yes it'll be blank for now)</li><li>Create a pull request from Milestone1 to main (don't complete/merge it yet, just have it in open status)</li><li>Copy in the latest Socket sample code from the most recent Socket Part example of the lessons</li><ol><li>Recommended Part 5 (clients should be having names at this point and not ids)</li><li><a href="https://github.com/MattToegel/IT114/tree/Module5/Module5">https://github.com/MattToegel/IT114/tree/Module5/Module5</a>&nbsp;<br></li></ol><li>Fix the package references at the top of each file (these are the only edits you should do at this point)</li><li>Git add/commit the baseline</li><li>Ensure the sample is working and fill in the below deliverables</li><ol><li>Note: The client commands likely are different in part 5 with the /name and /connect options instead of just connect</li></ol><li>Get the markdown content or the file and paste it into the milestone1.md file or replace the file with the downloaded version</li><li>Git add/commit/push all changes</li><li>Complete the pull request merge from step 5</li><li>Locally checkout main</li><li>git pull origin main</li></ol></td></tr></table>
<table><tr><td> <em>Deliverable 1: </em> Startup </td></tr><tr><td><em>Status: </em> <img width="100" height="20" src="https://user-images.githubusercontent.com/54863474/211707773-e6aef7cb-d5b2-4053-bbb1-b09fc609041e.png"></td></tr>
<tr><td><table><tr><td> <em>Sub-Task 1: </em> Add screenshot showing your server being started and running</td></tr>
<tr><td><table><tr><td><img width="768px" src="https://firebasestorage.googleapis.com/v0/b/learn-e1de9.appspot.com/o/assignments%2Fab2669%2F2023-10-24T00.54.07Deliverable1.png.webp?alt=media&token=c4d0d2b5-b999-410d-bebf-1c5214dffa15"/></td></tr>
<tr><td> <em>Caption:</em> <p>Output of Server Behavior<br></p>
</td></tr>
</table></td></tr>
<tr><td> <em>Sub-Task 2: </em> Add screenshot showing your client being started and running</td></tr>
<tr><td><table><tr><td><img width="768px" src="https://firebasestorage.googleapis.com/v0/b/learn-e1de9.appspot.com/o/assignments%2Fab2669%2F2023-10-24T00.57.20Deliverable1_Task2.png.webp?alt=media&token=ddf896fa-70cc-4673-bd96-2cb42a86b991"/></td></tr>
<tr><td> <em>Caption:</em> <p>Successful Client Connection<br></p>
</td></tr>
</table></td></tr>
<tr><td> <em>Sub-Task 3: </em> Briefly explain the connection process</td></tr>
<tr><td> <em>Response:</em> <p>The server listens on a specified port for incoming client connections. When a<br>client connects, it creates a new thread (ServerThread) to handle that client&#39;s communication.<br>The server has a list of chat rooms. The default room is the&quot;Lobby&quot;<br>room, and clients can join or create rooms. The server allows broadcasting messages<br>to all clients in all rooms. Clients can also send commands to the<br>server. The server side of the connection works by accepting incoming client connections,<br>creating separate threads for each client to manage their communication, and managing the<br>chat rooms and message broadcasting. The client code allows users to connect to<br>a chat server, send and receive messages, and execute commands such as connecting<br>to a server, quitting the application, and setting a display name. In terms<br>of the steps of the socket, the client starts a socket connection to<br>the server by using the /connect localhost:(connection) command. Once the connection is established,<br>the client sets up input and output streams for communication. It sends a<br>&quot;connect&quot; message to the server to confirm the connection which is now ready<br>to receive messages from the client through the established socket connection.<br></p><br></td></tr>
</table></td></tr>
<table><tr><td> <em>Deliverable 2: </em> Sending/Receiving </td></tr><tr><td><em>Status: </em> <img width="100" height="20" src="https://user-images.githubusercontent.com/54863474/211707773-e6aef7cb-d5b2-4053-bbb1-b09fc609041e.png"></td></tr>
<tr><td><table><tr><td> <em>Sub-Task 1: </em> Add screenshot(s) showing evidence related to the checklist</td></tr>
<tr><td><table><tr><td><img width="768px" src="https://firebasestorage.googleapis.com/v0/b/learn-e1de9.appspot.com/o/assignments%2Fab2669%2F2023-10-24T01.20.41Delivarable2_Task1.png.webp?alt=media&token=bf308f1e-ed28-4a0f-80b9-8ad8ecc07a20"/></td></tr>
<tr><td> <em>Caption:</em> <p>Two Clients Communicating with Server, while Clients are in separate Chat Rooms<br></p>
</td></tr>
</table></td></tr>
<tr><td> <em>Sub-Task 2: </em> Briefly explain how the messages are sent, broadcasted (sent to all connected clients), and received</td></tr>
<tr><td> <em>Response:</em> <p>In the chat application, clients send messages by wrapping them in a Payload<br>object and transmitting them to the server. The server broadcasts messages to all<br>connected clients by iterating through its client list and forwarding the message. Clients<br>receive messages through dedicated threads and process them depending if the messages are<br>connection updates, broadcasts, or direct messages.&nbsp;<br></p><br></td></tr>
</table></td></tr>
<table><tr><td> <em>Deliverable 3: </em> Disconnecting / Terminating </td></tr><tr><td><em>Status: </em> <img width="100" height="20" src="https://user-images.githubusercontent.com/54863474/211707773-e6aef7cb-d5b2-4053-bbb1-b09fc609041e.png"></td></tr>
<tr><td><table><tr><td> <em>Sub-Task 1: </em> Add screenshot(s) showing evidence related to the checklist</td></tr>
<tr><td><table><tr><td><img width="768px" src="https://firebasestorage.googleapis.com/v0/b/learn-e1de9.appspot.com/o/assignments%2Fab2669%2F2023-10-24T01.41.10Deliverable3_Task1.png.webp?alt=media&token=b30affd1-1941-4523-a2d1-62e4f8520e53"/></td></tr>
<tr><td> <em>Caption:</em> <p>Closed Server Successfully, with Second Client still running.<br></p>
</td></tr>
</table></td></tr>
<tr><td> <em>Sub-Task 2: </em> Briefly explain how the various disconnects/terminations are handled</td></tr>
<tr><td> <em>Response:</em> <ul><br><li>The server handles client disconnection with proper error handling and closes the<br>socket.&nbsp;<div>- The client program should gracefully handle server disconnect, possibly by attempting to<br>reconnect.&nbsp;</div><div>- The server removes disconnected clients and continues serving others.<br></div><br></li><br></ul><br></td></tr>
</table></td></tr>
<table><tr><td> <em>Deliverable 4: </em> Misc </td></tr><tr><td><em>Status: </em> <img width="100" height="20" src="https://user-images.githubusercontent.com/54863474/211707773-e6aef7cb-d5b2-4053-bbb1-b09fc609041e.png"></td></tr>
<tr><td><table><tr><td> <em>Sub-Task 1: </em> Add the pull request for this branch</td></tr>
<tr><td> <a rel="noreferrer noopener" target="_blank" href="https://github.com/ab2669/ab2669-it114-005/pull/12">https://github.com/ab2669/ab2669-it114-005/pull/12</a> </td></tr>
<tr><td> <em>Sub-Task 2: </em> Talk about any issues or learnings during this assignment</td></tr>
<tr><td> <em>Response:</em> <div>The sockets project was a lot. I understand how to establish communication between<br>clients. Going through Part 1 to Part 5 showed me how to make<br>an efficient chat system.&nbsp;<br></div><div><br></div><div><br></div><br></td></tr>
</table></td></tr>
<table><tr><td><em>Grading Link: </em><a rel="noreferrer noopener" href="https://learn.ethereallab.app/homework/IT114-005-F23/it114-milestone1/grade/ab2669" target="_blank">Grading</a></td></tr></table>
