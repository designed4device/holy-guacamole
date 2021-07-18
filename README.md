# Holy Guacamole!
This is a slack bot for recognizing your teammates.

## How to add to your slack workspace
    
1. Create a new slack app. This app is not distributed, so you will be creating a new app in your workspace instead of adding an exiting app to your workspace.
    1. Create the app in the desired slack workspace.

1. Configure Slack app's bot.
    1. Go to Features -> App Home.
    1. Enable "Allow users to send Slash commands and messages from the messages tab".
    
1. Configure Slack app's permissions.
    1. Go to Features -> OAuth & Permissions.
    1. Add the following "Bot Token Scopes":
        - app_mentions:read
        - channels:history
        - channels:read
        - chat:write
        - emoji:read
        - groups:history
        - groups:read
        - im:history
        - im:read
        - im:write
        - users:read
    
1. Install Slack app to your workspace.
    1. Go to Settings -> Basic Information.
    1. Click "Install to Workspace" button.
    1. Click "Allow".
    
1. Create a [mongodb cloud atlas](https://www.mongodb.com/cloud/atlas) cluster (free tier will work fine). This is the database where avocado receipts are stored.
    1. Create a mongodb cloud atlas account.
    1. Create a project (just give it a name).
    1. Create a database in the project.
        1. select free shared tier M0.
        1. everything else can be left default, unless you wanna be fancy.

1. Create a [Heroku](https://www.heroku.com/) app. This will host the api that slack communicates with. It will be connected to mongodb.
    1. All you need to do is give it a name and click create.
    
1. Add config vars to your Heroku app.
    1. Go to the settings page of your app.
    1. Click the "Revel Config Vars" button.
    1. Add the following required config vars: 
       | Key | Value |  
       | --- | ----- |  
       | BOT_USERID | <Your [Slack app's bot user ID](#slack-bot-user-id)> |  
       | MONGODB_URI | <Your [mongodb connection string](#mongodb-uri)> |  
       | SLACK_TOKEN_BOT | <Your [Slack app's Bot User OAuth Token](#slack-bot-user-token)> |  
       | SLACK_TOKEN_VERIFICATION | <Your [Slack app's Verification Token](#slack-app-verification-token)> |  
    1. Add the following optional config vars to enable milestone avocado functionality (when the milestone number of avocados have been given, a special message will be posted to the configured channel):
       | Key | Value |  
       | --- | ----- |  
       | MILECADO_CHANNEL | <The Slack channel id to post> |  
       | MILECADO_NEXT | <The number of avocados sent to trigger the milestone message e.g. 50000> |

1. Deploy the HolyGuacamole API to your Heroku app
    1. Clone this project.
       ```bash
       #navigate to your desired project location
       cd ~/workspace
       
       #clone the project in the current directory 
       git clone https://github.com/designed4device/holy-guacamole.git
       ```
    1. [Install the Heroku CLI](https://devcenter.heroku.com/articles/heroku-cli).
    1. Deploy using heroku git.
       ```bash
       #this will log you in by launching browser
       heroku login
       
       #navigate to your cloned holy-guacamole project
       cd ~/workspace/holy-guacamole 
       
       #add the heroku remote to git
       #replace your-heroku-app-name with your heroku app name
       heroku git:remote -a your-heroku-app-name
       
       #deploy
       git push heroku master
       ```

1. Configure Slack app's Event Subscription
    1. Go to Features -> Event Subscriptions.
    1. Enable events.
    1. Set the "Request URL" to `<your Heroku app url>/events`
        - Your Heroku app url can be found in the "Domains" section of your Heroku app settings.
        - e.g "https://my-heroku-app.herokuapp.com/events"
    1. Subscribe to bot events:
        - app_mentions
        - member_joined_channel
        - message.channels
        - message.groups
        - message.im
        - team_join
        - user_change
    1. Click "Save Changes" button.
    
### mongodb uri
1. Click the "Connect" button on your mongodb database deployment (DEPLOYMENT -> Databases).
1. Click "Allow connection from anywhere", then "Add IP Address". Optionally, you can instead configure to only allow access from the appropriate slack IP address.
1. Enter a secure username and password and click create (Be sure to save your username and password somewhere secure, you will need it later).
1. Click "Choose a connection method".
1. Click "Connect your application".
1. Select "Java" from the "DRIVER" dropdown.
1. Select "4.3 or later" from the "VERSION" dropdown.
1. Copy the connection string and replace <password> with the password you created previously.

### Slack bot user id
1. While viewing your slack workspace from a browser, open up a chat with your app (In the left menu where your channels and messages are, go to Apps -> Your App Name).
1. Click on the app's name at the top.
1. Copy the "Member ID" from the dialog that opened in previous step. It should look something like this "U028E073GR0".

### Slack bot user token
1. In your Slack App's configuration go to Features -> OAuth & Permissions.
1. Copy the "Bot User OAuth Token" from this page.

### Slack app verification token
1. In your Slack App's configuration go to Settings -> Basic Information.
1. Copy the "Verification Token" from the "App Credentials" section on this screen.