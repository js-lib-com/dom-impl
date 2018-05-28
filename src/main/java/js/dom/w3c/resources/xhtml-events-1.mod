<!-- ...................................................................... -->
<!-- XHTML Events Module .................................................. -->
<!-- file: xhtml-events-1.mod

     This is XHTML Events - the Events Module for XHTML and Friends,
     a redefinition of access to the DOM events model.

     Copyright 2000-2001 W3C (MIT, INRIA, Keio), All Rights Reserved.

     This DTD module is identified by the PUBLIC and SYSTEM identifiers:

       PUBLIC "-//W3C//ENTITIES XHTML Events 1.0//EN"
       SYSTEM "http://www.w3.org/TR/xhtml-events/DTD/xhtml-events-1.mod"

     Revisions:
     (none)
     ....................................................................... -->


<!-- XHTML Events-basic defines the essential components of this module -->

<!ENTITY % xhtml-events-basic.mod
     PUBLIC "-//W3C//ELEMENTS XHTML Events Basic 1.0//EN"
            "http://www.w3.org/TR/xhtml-events/DTD/xhtml-events-basic-1.mod" >
%xhtml-events-basic.mod;

<!-- Extend the onevent element with additional attributes -->

<!ATTLIST %xhtml-events.onevent.qname;
    eventsource      IDREF        #REQUIRED
    registerwith     IDREF        #IMPLIED
>

<!-- end of xhtml-events-1.mod -->