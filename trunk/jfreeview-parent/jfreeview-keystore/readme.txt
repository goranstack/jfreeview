The keystore.jks file is not checked in to Subversion. Create the keystore.jks file 
by invoking:mvn keytool:genkey

Java Web Start requires that all jars in the site/jnlp/lib folder are signed with the same keystore file. 
That means that when ever a new keystore file has been generated and you are building one of the web start projects, 
all other web start projects must be built as well since the web start projects use overlapping subsets
of the jars in the site/jnlp/lib folder.

The sharing of this keystore file among all Web Start projects is done by specifying
the relative path for example ../jfreeview-keystore/jnlp/keystore.jks.

This is not an ideal solution and some kind of remote keystore service would be nice.