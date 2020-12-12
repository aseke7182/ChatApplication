# ChatApplication
<h2>Team Members:</h2>
<table>
    <tr>
        <th>Name</th>
        <th>Email</th>
        <th>Github</th>
    </tr>
    <tr>
        <td>Madi Keshilbayev</td>
        <td>m.keshilbaev@gmail.com</td>
        <td><a href="https://github.com/mkeshilbaev">mkeshilbaev</a></td>
 	</tr>
    <tr>
        <td>Yerbolov Dauren</td>
        <td></td>
        <td><a href="https://github.com/dorenyerbolov">darenyerbolov</a></td>
    </tr>					
    <tr>
        <td>Mussayev Askar</td>
        <td>aseke7182@gmail.com</td>
        <td><a href="https://github.com/aseke7182/webdev2019">aseke7182</a></td>
    </tr>
</table>
<h2>About Project</h2>
<h4>We will implement a chat application, that has only one group chat. Users can join or leave this group. In case a particular user is not a member of this group chat - he is not allowed to perform any action within it. Messages are stored locally.</h4>
<p> 
	For this task we need the following actors: <br>
1) ChatGroupActor - actor for the main group chat, which users can join or leave<br>
2) ACLActor - actor that is responsible for access control: checks if a user can send/retrieve messages from a group chat<br>
3) UserActor - each user create separate UserActor. He subscribes to the events within a group chat and get messages from it by using Publish-Subscribe pattern
</p>