# muse
This project provides the implementation of the research paper Amine Mohamed Falek, Cristel Pelsser, Sebastien Julien, Fabrice Theoleyre, "MUSE: Multimodal Separators for Efficient Route Planning in Transportation Networks", Transportation Science , Volume 56 , Number 2, 2022, https://doi.org/10.1287/trsc.2021.1104.

Many algorithms compute shortest-path queries in mere microseconds on continental-scale networks. Most solutions are, however, tailored to either road or public transit networks in isolation. To fully exploit the transportation infrastructure, multimodal algorithms are sought to compute shortest paths combining various modes of transportation. Nonetheless, current solutions still lack performance to efficiently handle interactive queries under realistic network conditions where traffic jams, public transit cancelations, or delays often occur. We present a multimodal separators–based algorithm (MUSE), a new multimodal algorithm based on graph separators to compute shortest travel time paths. It partitions the network into independent, smaller regions, enabling fast and scalable preprocessing. The partition is common to all modes and independent of traffic conditions so that the preprocessing is only executed once. MUSE relies on a state automaton that describes the sequence of modes to constrain the shortest path during the preprocessing and the online phase. The support of new sequences of mobility modes only requires the preprocessing of the cliques, independently for each partition. We also augment our algorithm with heuristics during the query phase to achieve further speedups with minimal effect on correctness. We provide experimental results on France’s multimodal network containing the pedestrian, road, bicycle, and public transit networks.

MUSE is multimodal by nature, and we consider connecting lines for public transportation.

![MUSE behavior](https://fabrice.theoleyre.cnrs.fr/uploads/Software/muse.jpg)

MUSE considers multiple layers (one per transport mode). Thus, only one layer is updated when conditions change (e.g., traffic jam). Thus, MUSE scales well.

![layers of MUSE](https://fabrice.theoleyre.cnrs.fr/uploads/Software/layers.jpg)
