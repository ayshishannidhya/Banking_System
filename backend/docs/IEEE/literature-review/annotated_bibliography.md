# Literature Review — Annotated Bibliography
## IEEE Research: Performance Evaluation of Event-Driven Banking Microservices

> **Instructions**: For each paper, fill in the structured fields below.
> Only include papers you have actually read and verified.
> Target: 60–100 papers from IEEE Xplore, ACM DL, SpringerLink, ScienceDirect, Google Scholar.

---

## Category 1: Microservice Architecture & Communication Patterns

### Paper 1: Building Microservices: Designing Fine-Grained Systems
- **Authors**: Sam Newman
- **Year**: 2015
- **Venue**: O'Reilly Media (Book)
- **Problem Statement**: How to decompose monolithic applications into microservices with proper inter-service communication.
- **Methodology**: Practitioner-oriented design patterns and case studies.
- **Architecture**: Service-per-capability decomposition with REST/messaging.
- **Experimental Setup**: N/A (practitioner guide, no experiments).
- **Evaluation Metrics**: N/A
- **Key Results**: Defined canonical patterns for microservice communication (sync vs async), service discovery, and deployment.
- **Limitations**: No empirical performance evaluation; prescriptive rather than evidence-based.
- **Research Gap**: Does not quantify performance differences between communication paradigms.
- **Relevance to Our Work**: Foundational reference for microservice design decisions.

---

### Paper 2: Microservices: Yesterday, Today, and Tomorrow
- **Authors**: Dragoni, N., Giallorenzo, S., et al.
- **Year**: 2017
- **Venue**: Springer — Present and Ulterior Software Engineering
- **DOI**: 10.1007/978-3-319-67425-4_12
- **Problem Statement**: Comprehensive survey of microservice architecture challenges and open research problems.
- **Methodology**: Systematic literature review.
- **Architecture**: General microservice patterns.
- **Experimental Setup**: N/A (survey).
- **Evaluation Metrics**: N/A
- **Key Results**: Identified inter-service communication as a critical open problem; called for empirical comparisons.
- **Limitations**: Survey paper with no original experiments.
- **Research Gap**: Explicitly identifies the need for empirical performance studies.
- **Relevance to Our Work**: Motivates our empirical comparison.

---

### Paper 3: A Systematic Mapping Study in Microservice Architecture
- **Authors**: Alshuqayran, N., Ali, N., Evans, R.
- **Year**: 2016
- **Venue**: IEEE SOCA 2016
- **DOI**: 10.1109/SOCA.2016.15
- **Problem Statement**: Systematic mapping of microservice research to identify trends and gaps.
- **Methodology**: Systematic mapping study of 33 primary studies.
- **Key Results**: REST was dominant communication mechanism; limited empirical evidence on alternatives.
- **Limitations**: Small sample of primary studies; rapid growth of field since publication.
- **Research Gap**: Insufficient empirical comparison of messaging alternatives to REST.
- **Relevance to Our Work**: Confirms the gap in comparative communication studies.

---

### Paper 4: Evaluating the Monolithic and the Microservice Architecture Pattern
- **Authors**: Villamizar, M., et al.
- **Year**: 2015
- **Venue**: IEEE 10th Computing Colombian Conference
- **DOI**: 10.1109/ColumbianCC.2015.7333476
- **Problem Statement**: Comparing monolith vs microservice deployment on AWS.
- **Methodology**: Empirical deployment comparison on Amazon EC2.
- **Architecture**: Monolithic Java app vs microservice decomposition.
- **Experimental Setup**: AWS EC2 instances, Apache JMeter.
- **Evaluation Metrics**: Response time, throughput, infrastructure cost.
- **Key Results**: Comparable response times; microservices required more infrastructure but better scalability.
- **Limitations**: Only compared monolith vs microservice, not communication paradigms within microservices.
- **Research Gap**: Did not compare REST vs messaging within the microservice architecture.
- **Relevance to Our Work**: Validates microservice deployment approach but leaves communication comparison unaddressed.

---

## Category 2: Apache Kafka & Message Brokers

### Paper 5: Kafka: A Distributed Messaging System for Log Processing
- **Authors**: Kreps, J., Narkhede, N., Rao, J.
- **Year**: 2011
- **Venue**: NetDB Workshop
- **Problem Statement**: Building a unified, high-throughput messaging system for log processing at LinkedIn.
- **Methodology**: System design and implementation with benchmarks.
- **Architecture**: Distributed commit log with partitioned topics.
- **Experimental Setup**: LinkedIn production cluster.
- **Evaluation Metrics**: Throughput (messages/sec), latency.
- **Key Results**: Kafka achieved 2x throughput of ActiveMQ for production workloads.
- **Limitations**: Original paper predates KRaft mode and modern Kafka features.
- **Research Gap**: Focused on log processing; banking transaction workloads not evaluated.
- **Relevance to Our Work**: Foundational Kafka reference.

---

### Paper 6: Kafka versus RabbitMQ: A Comparative Study
- **Authors**: Dobbelaere, P., Sheykh Esmaili, K.
- **Year**: 2017
- **Venue**: ACM DEBS 2017
- **DOI**: 10.1145/3093742.3093908
- **Problem Statement**: Systematic comparison of Kafka and RabbitMQ.
- **Methodology**: Feature comparison and microbenchmarks.
- **Architecture**: Standalone producer-consumer setups.
- **Experimental Setup**: Single-node benchmarks with varying message sizes.
- **Evaluation Metrics**: Throughput, latency, feature comparison.
- **Key Results**: Kafka achieved up to 15x higher throughput; RabbitMQ had lower latency for small messages.
- **Limitations**: Standalone benchmarks without application framework overhead; not embedded in a real application.
- **Research Gap**: Does not evaluate within a Spring Boot microservice context with database operations.
- **Relevance to Our Work**: Directly informs our Kafka vs RabbitMQ comparison, but we add application-level context.

---

### Paper 7: A Performance Evaluation of Apache Kafka in Support of Big Data Streaming Applications
- **Authors**: Le Noac'h, P., Costan, A., Bougé, L.
- **Year**: 2017
- **Venue**: IEEE International Conference on Big Data
- **DOI**: 10.1109/BigData.2017.8258548
- **Problem Statement**: Evaluating Kafka performance characteristics under Big Data workloads.
- **Methodology**: Multi-broker benchmarks with varying configurations.
- **Experimental Setup**: Multi-node cluster, varying partitions and message sizes.
- **Evaluation Metrics**: Throughput, latency, scalability with brokers/partitions.
- **Key Results**: Kafka throughput scales near-linearly with partitions up to CPU core count.
- **Limitations**: Big Data focus; not transactional banking workloads.
- **Research Gap**: Partition scaling behavior in Spring Boot service context unknown.
- **Relevance to Our Work**: Informs our Kafka partition tuning experiments.

---

## Category 3: Performance Evaluation of Microservice Systems

### Paper 8: An Open-Source Benchmark Suite for Microservices (DeathStarBench)
- **Authors**: Gan, Y., et al.
- **Year**: 2019
- **Venue**: ACM ASPLOS 2019
- **DOI**: 10.1145/3297858.3304013
- **Problem Statement**: Need for representative, open-source microservice benchmarks.
- **Methodology**: Design and evaluation of 5 microservice applications as benchmarks.
- **Architecture**: Social network, hotel reservation, media, e-commerce microservices.
- **Experimental Setup**: CloudLab cluster, Locust load generator.
- **Evaluation Metrics**: Latency (p50/p95/p99), throughput, CPU utilization, cache hit ratio.
- **Key Results**: End-to-end latency dominated by queuing delays at high utilization; tail latency 10x median.
- **Limitations**: No Kafka integration; all synchronous RPC communication.
- **Research Gap**: Banking domain not represented; no event-driven communication comparison.
- **Relevance to Our Work**: Methodology model for statistically rigorous microservice benchmarking.

---

### Paper 9: μTune: Auto-Tuned Threading for OLDI Microservices
- **Authors**: Sriraman, A., Wenisch, T. F.
- **Year**: 2018
- **Venue**: USENIX OSDI 2018
- **Problem Statement**: Reducing tail latency in online data-intensive microservices.
- **Methodology**: Automated thread pool tuning with feedback control.
- **Key Results**: p99 latency can be 10x the median; resource contention between co-located services.
- **Limitations**: Focuses on thread tuning, not communication paradigm comparison.
- **Research Gap**: Does not compare messaging alternatives.
- **Relevance to Our Work**: Informs tail latency analysis methodology.

---

## Category 4: Banking & Financial System Architecture

### Paper 10: Microservices from Theory to Practice (IBM Redbooks)
- **Authors**: Daya, S., et al.
- **Year**: 2016
- **Venue**: IBM Redbooks
- **Problem Statement**: Practical guidance for implementing microservices in enterprise (including financial) contexts.
- **Methodology**: Case study-based implementation guide.
- **Key Results**: Saga pattern for distributed transactions; Event Sourcing for audit trails.
- **Limitations**: Prescriptive guidance without empirical evaluation.
- **Research Gap**: No performance benchmarks for financial transaction patterns.
- **Relevance to Our Work**: Validates Saga and Event Sourcing patterns used in our architecture.

---

## Template for Additional Papers

### Paper N: [Title]
- **Authors**: [Authors]
- **Year**: [Year]
- **Venue**: [Journal/Conference Name]
- **DOI**: [DOI or URL]
- **Problem Statement**: [What problem does this paper address?]
- **Methodology**: [What research method is used?]
- **Architecture**: [What system architecture is described?]
- **Experimental Setup**: [Hardware, software, benchmarking tools]
- **Dataset**: [What data or workload was used?]
- **Evaluation Metrics**: [What was measured?]
- **Key Results**: [Main findings — use numbers when available]
- **Limitations**: [What are the paper's weaknesses?]
- **Research Gap**: [What does this paper leave unresolved?]
- **Relevance to Our Work**: [How does this relate to our study?]

---

## Comparison Summary Table

| Study | Year | REST | RabbitMQ | Kafka | Banking | Fault Tol. | Stat. Rigor | Open Source |
|-------|------|------|----------|-------|---------|------------|-------------|-------------|
| Newman [1] | 2015 | ✓ | -- | -- | -- | -- | -- | -- |
| Dragoni et al. [2] | 2017 | -- | -- | -- | -- | -- | -- | -- |
| Alshuqayran et al. [3] | 2016 | ✓ | -- | -- | -- | -- | -- | -- |
| Villamizar et al. [4] | 2015 | ✓ | -- | -- | -- | -- | -- | -- |
| Kreps et al. [5] | 2011 | -- | -- | ✓ | -- | -- | -- | ✓ |
| Dobbelaere & Esmaili [6] | 2017 | -- | ✓ | ✓ | -- | -- | Partial | -- |
| Le Noac'h et al. [7] | 2017 | -- | ✓ | ✓ | -- | -- | ✓ | -- |
| Gan et al. [8] | 2019 | ✓ | -- | -- | -- | -- | ✓ | ✓ |
| Sriraman & Wenisch [9] | 2018 | ✓ | -- | -- | -- | -- | ✓ | -- |
| Daya et al. [10] | 2016 | ✓ | -- | -- | ✓ | -- | -- | -- |
| **This Study** | **2026** | **✓** | **✓** | **✓** | **✓** | **✓** | **✓** | **✓** |

---

## Search Strategy

### Search Terms Used
- "microservice" AND "Kafka" AND "performance"
- "event-driven architecture" AND "banking"
- "message broker" AND "comparison" AND "throughput"
- "microservice" AND "REST" AND "latency"
- "Apache Kafka" AND "Spring Boot" AND "benchmark"
- "distributed systems" AND "financial" AND "scalability"
- "RabbitMQ" AND "Kafka" AND "comparison"
- "microservice" AND "fault tolerance"

### Databases Searched
- IEEE Xplore (primary)
- ACM Digital Library
- SpringerLink
- ScienceDirect
- Google Scholar

### Inclusion Criteria
- Published within last 7 years (2019–2026 preferred, 2015+ accepted)
- Peer-reviewed journal or conference paper
- Addresses microservice communication, event-driven architecture, or distributed system performance
- Written in English

### Exclusion Criteria
- Non-peer-reviewed blog posts or white papers (unless from major technology companies)
- Studies with no empirical component (unless foundational surveys)
- Duplicate publications
