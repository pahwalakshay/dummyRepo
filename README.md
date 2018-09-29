# dummyRepo

All Latest APIs
In this question, you have to write a well-designed program in Java8 (or newer; preferably using Java8 features like streams, lambdas, Optional). 
Your program must be well-designed, with appropriate classes and methods, as well as correct (i.e. producing the correct output for various different input cases) 
and efficient (i.e. scalable).

A company has a number of mobile apps that they sell to customers, and each app uses one or more REST APIs. All REST APIs are given version numbers like 
v1, v2, v3, ... etc. which are incremented each time the REST API team releases a new version of the API to the app teams.

Consider a file input.txt which contains information about which versions of the various APIs are being used by the current release of each app of the company:


Mail App, Authentication API, v6
Video Call App, Authentication API, v7
Mail App, Data Storage API, v10
Chat App, Data Storage API, v11
Mail App, Search API, v6
Chat App, Authentication API, v8
Chat App, Presence API, v2
Video Call App, Data Storage API, v11
Video Call App, Video Compression API, v3
Each line of this file consists of 3 comma-separated fields. The first field is a app name, the second field is a API name, and the third field is the API 
version number used by that app. The example given above indicates that the Mail App, uses version v7 of the Authentication API, and the Video Call App uses 
version v7 of the Authentication API, and the Mail App also uses version v10 of the Data Storage API, and so on. For the purposes of this program, assume that 
all version numbers are of the form v<digits> where <digits> represents one or more decimal digits.

Write a program (using Java8 or newer) that reads this file, figures out which is the latest version number of each API, and then prints the names of apps which 
are using the latest version of all the APIs that it uses. Special Note: if a API has only one version, then that version is obviously considered the latest 
version.

Thus, in case of the example input given above, the output of your program should be:

Chat App
because Chat App is using 3 APIs, and it is using the latest versions of all 3 of those APIs. Mail App is not listed, because it is using oldest versions of 
the Authentication API as well as the Data Storage API, and Video Call App is not listed because it is using an older version of the Authentication API. 
Please read the example carefully to understand exactly what you're expected to do.

Important notes:

To make things easier for you, we have already written a starter program which reads input.txt line-by-line, and does nothing. It simply prints the number of 
lines to output.txt. You need ti just modify this program to do the required processing, and change what is printed to output.txt as shown above.
Ideally, your program should use Java8 features like streams, lambdas, Optional types etc
Your program must be well-designed, with appropriate classes and methods
You must submit a working program that produces the correct output for various different input cases
Your program must be efficient (i.e. scalable).
Upload a zip file containing the source code of your program
