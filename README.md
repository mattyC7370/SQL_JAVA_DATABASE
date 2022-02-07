# Database-Project

Project 1 for CSCE 315 Programming Languages

## Adding credentials to access the database

Create a file named Credentials.java in your project folder.
Open the file and paste the following:
`public final class Credentials  {
    public final String user = "/username/";
    public final String pswd = "/password/";
}`
And replace the /username/ field with your username, and /password/ field with your password

## To create Javadoc for this module

In a terminal window run
`javadoc QUERY.java DATA.java SELECTOR.java SUB_COND.java SQLWrapper.java  -d <Directory to put website files>`

### Enabling debug

In the source code, add `ProjectGlobals.DEBUG = true;` to enable debug statments. This can be disabled later in the code by setting debug to false
