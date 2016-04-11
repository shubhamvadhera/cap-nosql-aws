#CAP Theorem AP/CP Replication Key/Value NoSQL REST Database

##Project Demo on YouTube:
CAP Theorem - AP implementation on Amazon AWS

https://youtu.be/0HpfOYEh8H4

CAP Theorem - CP implementation on Amazon AWS - Part 2

https://youtu.be/nM1x990jGAw


##Preface:

This project has been extended from an existing key/value database store written in Java and using the Shore DB API Design. 
The starter source code is available in GitHub at:

https://github.com/paulnguyen/data/tree/master/nosql


##Objectives:

* Implement a "three node" instance of the database on AWS EC2 free-tier instances.
* Design should support both -  either AP or CP based replication at a time
  * This project is a Master/Slave implementation which supports both AP and CP.
* All three instances must be public accessible (via public IP) and must be in different sub-nets in the same VPC.
* External access must be via REST API and should expose full CRUD operations for Key/Value documents.
* Replication must be between the nodes using the internal network
* The document format must be in JSON

##Prerequisites:
* Requires Java 8
* Requires Amazon AWS account
* Install Apache server for load balancer health checks on HTTP port 80

##Setup on Amazon AWS:
* Launch and instance, install Java 8, apache server and copy project code to the instance.
* Try running the code by executing the script run-as-master-ap.sh just to make sure that there are no compilation errors.
* If you see "Starting the internal HTTP server on port 8080" and "Starting the internal HTTP server on port 8081", set up was successful.
* Create an AMI of this instance which would be used to launch other 2 instances.
* Setup a VPC containing three instances, each in separate subnets.
* Create separate security groups for Slave1, Slave2 and Master.
* Create  a "Jump Box" in your VPC so that you can SSH into each of the Nodes from the Jump Box.
* Open the following ports:
  * HTTP 80 - for load balancer health checks
  * Custom TCP 8080 - for Master and Slave only
  * SSH 22 - for SSH
  * Custom TCP 8081 - for all IP
* Setup an Elastic Load Balancer for your Three Nodes (1 Master and 2 Slaves) and associate a Public Elastic IP to the ELB.
* All External REST API calls will be made to the ELB via the ELB Host Name or Public Elastic IP.
* The Network Communication between Nodes for Replication and Administration would be on Port 8080.
* Modify the Security Group Rules as appropriate to allow the 8080  HTTP Traffic between the Nodes.
* To "Start Up" the Nodes, SSH into each Node and start the nodes with the Private IPs of itself and the other Two Nodes. 

### Replication for CP:
* If running as CP, external requests will come in via ELB and can be serviced by any node.  Write operations are then forwarded to the Master from a Slave node.
* In Partition Mode, slaves within Partition don't allow any reads or writes.

##### To run the project as CP:
* Run run-as-master-cp.sh on Master instance.
* Run run-as-slave.1-cp.sh on Slave 1.
* Run run-as-slave.2-cp.sh on Slave 2.

### Replication for AP:
* In Partition Mode, Slaves don't allow a "write" if communications to Master is down.  Read operations are allowed from Slave Node (although data may be stale).
* In Partition Mode on the Master Node, read and writes are allowed even-though replication will be delayed (until network comes back up).

##### To run the project as AP:
* Run run-as-master-ap.sh on Master instance.
* Run run-as-slave.1-ap.sh on Slave 1.
* Run run-as-slave.2-ap.sh on Slave 2.

## Other Implementation Notes:
JSON response includes details such as: 
* The Node IP servicing the request.
* The Data Version Metadata for the response.

External REST API calls include:
* Read (Key) => Return Value or Values
* Write (Key, Value) => Return Status (Error / Success)
* Update (Key, New Value) => Return Status (Error / Success)
* Delete (Key) => Return Status (Error / Success)
