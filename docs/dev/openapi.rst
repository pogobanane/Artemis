OpenAPI
=======

An OpenAPI specification file can be generated from the SpringBoot project.
This specification file can be used to generate the client sdk code.


Generation of the OpenAPI specification
---------------------------------------

1) start a DB instance for Artemis
2) run the following gradle command:
   .. code::

       gradle clean generateOpenApiDocs

