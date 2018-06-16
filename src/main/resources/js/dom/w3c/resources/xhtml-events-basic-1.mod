<!-- ...................................................................... -->
<!-- Basic XHTML Events Module  ............................................ -->
<!-- file: xhtml-events-basic-1.mod

     This is Basic XHTML Events - the Basic Events Module for XHTML and Friends,
     a redefinition of access to the DOM events model.

     Copyright 2000-2001 W3C (MIT, INRIA, Keio), All Rights Reserved.

     This DTD module is identified by the PUBLIC and SYSTEM identifiers:

       PUBLIC "-//W3C//ENTITIES XHTML Basic XHTML Events 1.0//EN"
       SYSTEM "http://www.w3.org/TR/xhtml-events/DTD/xhtml-events-basic-1.mod"

     Revisions:
     (none)
     ....................................................................... -->


<!ENTITY % xhtml-events.onevent.content "((%xhtml-events.action.qname;,%xhtml-events.stopevent.qname;?)|
                                    (%xhtml-events.do.qname;,%xhtml-events.stopevent.qname;?)|
                        (script.qname;,%xhtml-events.stopevent.qname;?)|
                     %xhtml-events.stopevent.qname;)" >

<!ELEMENT %xhtml-events.onevent.qname; %xhtml-events.onevent.content;>
<!ATTLIST %xhtml-events.onevent.qname;
    id               ID           #IMPLIED
    onphase          (capturing|bubbling|target) #IMPLIED
    type             NMTOKEN      #REQUIRED
>

<!ENTITY % xhtml-events.action.content EMPTY>
<!ELEMENT %xhtml-events.action.qname; %xhtml-events.action.content;>
<!ATTLIST %xhtml-events.action.qname;
    id       ID             #IMPLIED
    href     %URI;           #REQUIRED
    type     %ContentType;   #IMPLIED
>

<!ENTITY % xhtml-events.do.content ANY>
<!ELEMENT action %xhtml-events.do.content;>
<!ATTLIST %xhtml-events.do.qname;
    id       ID             #IMPLIED
>

<!ENTITY % xhtml-events.stopevent.content EMPTY>
<!ELEMENT %xhtml-events.stopevent.qname; %xhtml-events.stopevent.content;>
<!ATTLIST %xhtml-events.stopevent.qname;
    id       ID             #IMPLIED
>

<!-- end of xhtml-xhtml-events-1.mod -->