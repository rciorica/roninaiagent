This app must fulfill the following:
Create a webapp (with the possibility to easily integrate it in desktop or mobile app) which serves as AI agent. 
The name of the app is Ronin, from the Japanese term used for exiled samurai. 
Ronin is the master AI Agent which controls and gets responses from LLMs and integrates all components
All LLMs need to respond to Ronin
Users login into the app and start building projects. Different free AI LLMs can be used, from a list of most recommended depending on the project phase: frontend, backend services, db connection, cloud deployment, etc. The user is only allowed to use the free tokens available in each LLM, when those are consumed, they are switched automatically to the next best LLM in the category based on the project phase mentioned above, which a small warning indicator this has occurred.
Every project created by the user is tested for completion and correctness automatically.
The users are ranked by the number of completed projects and receive levels of kyu similar to the ranking used when obtaining the belts in kyokushin.

Keep code simple
Keep code clean
Test code after chunks of steps are done
Ensure integration frontend-backend works after chunks of steps are done
Think analytical and goal oriented
Use design patterns and SOLID principles
run tests 
Remember : I’m switching to cmd.exe for the npm test run so PowerShell script restrictions don’t block execution.

cd "c:\cygwin64\home\BTCES\roninaiagent\backend" ; mvn -q test
remember how to run the tests correctly