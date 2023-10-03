<table><tr><td> <em>Assignment: </em> IT114 - Number Guesser</td></tr>
<tr><td> <em>Student: </em> Andrew Boksz (ab2669)</td></tr>
<tr><td> <em>Generated: </em> 10/2/2023 11:32:06 PM</td></tr>
<tr><td> <em>Grading Link: </em> <a rel="noreferrer noopener" href="https://learn.ethereallab.app/homework/IT114-005-F23/it114-number-guesser/grade/ab2669" target="_blank">Grading</a></td></tr></table>
<table><tr><td> <em>Instructions: </em> <ol><li>Create the below branch name</li><li>Implement the NumberGuess4 example from the lesson/slides</li><ol><li><a href="https://gist.github.com/MattToegel/aced06400c812f13ad030db9518b399f">https://gist.github.com/MattToegel/aced06400c812f13ad030db9518b399f</a><br></li></ol><li>Add/commit the files as-is from the lesson material (this is the base template)</li><li>Pick two (2) of the following options to implement</li><ol><li>Display higher or lower as a hint after a wrong guess</li><li>Implement anti-data tampering of the save file data (reject user direct edits)</li><li>Add a difficulty selector that adjusts the max strikes per level</li><li>Display a cold, warm, hot indicator based on how close to the correct value the guess is (example, 10 numbers away is cold, 5 numbers away is warm, 2 numbers away is hot; adjust these per your preference)</li><li>Add a hint command that can be used once per level and only after 2 strikes have been used that reduces the range around the correct number (i.e., number is 5 and range is initially 1-15, new range could be 3-8 as a hint)</li><li>Implement separate save files based on a "What's your name?" prompt at the start of the game</li></ol><li>Fill in the below deliverables</li><li>Create an m3_submission.md file and fill in the markdown from this tool when you're done</li><li>Git add/commit/push your changes to the HW branch</li><li>Create a pull request to main</li><li>Complete the pull request</li><li>Grab the link to the m3_submission.md from the main branch and submit that direct link to github</li></ol></td></tr></table>
<table><tr><td> <em>Deliverable 1: </em> Implementation 1 (one of the picked items) </td></tr><tr><td><em>Status: </em> <img width="100" height="20" src="https://user-images.githubusercontent.com/54863474/211707773-e6aef7cb-d5b2-4053-bbb1-b09fc609041e.png"></td></tr>
<tr><td><table><tr><td> <em>Sub-Task 1: </em> Two Screenshots: Add a screenshot demonstrating the feature during runtime; Add a screenshot (or so) of the snippets of code that implement the feature</td></tr>
<tr><td><table><tr><td><img width="768px" src="https://firebasestorage.googleapis.com/v0/b/learn-e1de9.appspot.com/o/assignments%2Fab2669%2F2023-10-03T02.42.51Module3_Deliverable1.png.webp?alt=media&token=1b6efe16-9dc3-4a17-8c02-f0512f106bc7"/></td></tr>
<tr><td> <em>Caption:</em> <ol>
<li>Displays higher or lower as a hint after a wrong guess<br></li>
</ol>
</td></tr>
<tr><td><img width="768px" src="https://firebasestorage.googleapis.com/v0/b/learn-e1de9.appspot.com/o/assignments%2Fab2669%2F2023-10-03T03.08.05Module3_Deliverable1_OUTPUT.png.webp?alt=media&token=7f2e8b6f-5324-4034-ad57-e9e3e73315a5"/></td></tr>
<tr><td> <em>Caption:</em> <ol>
<li>Displays higher or lower as a hint after a wrong guess [OUTPUT]<br></li>
</ol>
</td></tr>
</table></td></tr>
<tr><td> <em>Sub-Task 2: </em> Briefly explain the logic behind your implementation</td></tr>
<tr><td> <em>Response:</em> <p>I knew that I needed to implement into method proccessGuess because processGuess was<br>the block of code that determined if user input was correct about the<br>predicted value. In order to provide a hint if the user&#39;s guess was<br>too high or low, I would simply have to replace the original else<br>statement (which would output &quot;That&#39;s wrong&quot;) with the else if and else statements<br>of guess is too low (if guess &lt; number) and guess too high<br>(else) respectively. The strike count is noted after the else if and else<br>statements and the if statement below it considers if the user has used<br>all of their strikes (the code was already in the original template).&nbsp;<br></p><br></td></tr>
</table></td></tr>
<table><tr><td> <em>Deliverable 2: </em> Implementation 2 (one of the picked items) </td></tr><tr><td><em>Status: </em> <img width="100" height="20" src="https://user-images.githubusercontent.com/54863474/211707773-e6aef7cb-d5b2-4053-bbb1-b09fc609041e.png"></td></tr>
<tr><td><table><tr><td> <em>Sub-Task 1: </em> Two Screenshots: Add a screenshot demonstrating the feature during runtime; Add a screenshot (or so) of the snippets of code that implement the feature</td></tr>
<tr><td><table><tr><td><img width="768px" src="https://firebasestorage.googleapis.com/v0/b/learn-e1de9.appspot.com/o/assignments%2Fab2669%2F2023-10-03T03.09.14Module3_Deliverable2.png.webp?alt=media&token=70ae62ff-575d-47a0-96d6-debe284263c5"/></td></tr>
<tr><td> <em>Caption:</em> <ol start="3">
<li>Add a difficulty selector that adjusts the max strikes per level<br></li>
</ol>
</td></tr>
<tr><td><img width="768px" src="https://firebasestorage.googleapis.com/v0/b/learn-e1de9.appspot.com/o/assignments%2Fab2669%2F2023-10-03T03.09.29Module3_Deliverable_OUTPUT.png.webp?alt=media&token=3d2a8f82-2214-4a21-90b9-24778f21a786"/></td></tr>
<tr><td> <em>Caption:</em> <ol start="3">
<li>Add a difficulty selector that adjusts the max strikes per level [OUTPUT<br></li>
</ol>
</td></tr>
</table></td></tr>
<tr><td> <em>Sub-Task 2: </em> Briefly explain the logic behind your implementation</td></tr>
<tr><td> <em>Response:</em> <p>I added a new method to set the difficulty level. First, I created<br>a menu that lists the levels (easy medium, hard). Since there were only<br>three specific choices the user could make, the switch and case statement seemed<br>like the most logical things for me to do. If the user inputs<br>anything other than 1,2,3, then the default case will display an invalid choice<br>and start the game on medium difficulty. After I made my method, I<br>declared it in the start method, before loadState() occurs (when the actual game<br>starts).&nbsp;<br></p><br></td></tr>
</table></td></tr>
<table><tr><td> <em>Deliverable 3: </em> Misc </td></tr><tr><td><em>Status: </em> <img width="100" height="20" src="https://user-images.githubusercontent.com/54863474/211707773-e6aef7cb-d5b2-4053-bbb1-b09fc609041e.png"></td></tr>
<tr><td><table><tr><td> <em>Sub-Task 1: </em> Add a link to the related pull request of this hw</td></tr>
<tr><td> <a rel="noreferrer noopener" target="_blank" href="https://github.com/ab2669/ab2669-it114-005/pull/8">https://github.com/ab2669/ab2669-it114-005/pull/8</a> </td></tr>
<tr><td> <em>Sub-Task 2: </em> Discuss anything you learned during this lesson/hw or any struggles you had</td></tr>
<tr><td> <em>Response:</em> <p>The homework for this week seemed pretty straightforward. I enjoyed how there were<br>so many methods that there was nothing really to input in the main<br>method. Organized code is satisfying.&nbsp;<br></p><br></td></tr>
</table></td></tr>
<table><tr><td><em>Grading Link: </em><a rel="noreferrer noopener" href="https://learn.ethereallab.app/homework/IT114-005-F23/it114-number-guesser/grade/ab2669" target="_blank">Grading</a></td></tr></table>
