OpenAPI
=======

An OpenAPI specification file can be generated from the SpringBoot project.
This specification file can be used to generate the client sdk code.


Generation of the OpenAPI specification
---------------------------------------

1) start a DB instance for Artemis
2) make sure a start-able instance of Artemis is configured in your application-local.yml as the local profile is loaded
   to generate the OpenAPI specification (so far we use an Artemis instance without any external system for the
   generation procedure)
3) run the following gradle command:
   .. code::

       gradle clean generateOpenApiDocs

