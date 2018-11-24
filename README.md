[![Codacy Badge](https://api.codacy.com/project/badge/Grade/cf0c67031ba347779e204b63969a5d73)](https://app.codacy.com/app/renespeck/Ocelot?utm_source=github.com&utm_medium=referral&utm_content=dice-group/Ocelot&utm_campaign=Badge_Grade_Dashboard)
[![Build Status](https://travis-ci.org/dice-group/Ocelot.svg?branch=master)](https://travis-ci.org/dice-group/Ocelot)

### On Extracting Relations using Distributional Semantics and a Tree Generalization

Extracting relations out of unstructured text is essential for a wide range of applications. Minimal human effort, scalability and high precision are desirable characteristics. We introduce a distant supervised closed relation extraction approach based on distributional semantics and a tree generalization. Our approach uses training data obtained from a reference knowledge base to derive dependency parse trees that might express a relation. It then uses a novel generalization algorithm to construct dependency tree patterns for the relation. Distributional semantics are used to eliminate false candidate patterns. We evaluate the performance in experiments on a large corpus using ninety target relations. Our evaluation results suggest that our approach achieves a higher precision than two state-of-the-art systems. Moreover, our results also underpin the scalability of our approach. Our open source implementation can be found at https://github.com/dice-group/Ocelot.

### How to cite

```Tex
@InProceedings{
  author="Speck, Ren{\'e} and Ngomo Ngonga, Axel-Cyrille",
  title="On Extracting Relations Using Distributional Semantics and a Tree Generalization",
  booktitle="Knowledge Engineering and Knowledge Management",
  year="2018",
  publisher="Springer International Publishing",
  isbn="978-3-030-03667-6"
}
```

#### Requirements
Java 8, Maven 3

#### Bugs
Found a :bug: bug? [Open an issue](https://github.com/dice-group/Ocelot/issues/new) with some [emojis](http://emoji.muan.co). Issues without emojis are not valid. :trollface:
