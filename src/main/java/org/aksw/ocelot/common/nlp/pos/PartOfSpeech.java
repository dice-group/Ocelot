package org.aksw.ocelot.common.nlp.pos;

/**
 * Part-of-speech tags used in the Penn Treebank Project.<br>
 * <br>
 * <code>
CC   Coordinating conjunction
CD   Cardinal number
DT   Determiner
EX   Existential there
FW   Foreign word
IN   Preposition or subordinating conjunction
JJ   Adjective
JJR  Adjective, comparative
JJS  Adjective, superlative
LS   List item marker
MD   Modal
NN   Noun, singular or mass
NNS  Noun, plural
NNP  Proper noun, singular
NNPS Proper noun, plural
PDT  Predeterminer
POS  Possessive ending
PRP  Personal pronoun
PRP$ Possessive pronoun
RB   Adverb
RBR  Adverb, comparative
RBS  Adverb, superlative
RP   Particle
SYM  Symbol
TO   to
UH   Interjection
VB   Verb, base form
VBD  Verb, past tense
VBG  Verb, gerund or present participle
VBN  Verb, past participle
VBP  Verb, non-3rd person singular present
VBZ  Verb, 3rd person singular present
WDT  Wh-determiner
WP   Wh-pronoun
WP$  Possessive wh-pronoun
WRB  Wh-adverb
 </code>
 *
 * @author Ren&eacute; Speck <speck@informatik.uni-leipzig.de>
 *
 */
public enum PartOfSpeech {

  // CC Coordinating conjunction
  CONJUNCTION_COORDINATING("CC"),

  // CD Cardinal number
  CARDINAL_NUMBER("CD"),

  // DT Determiner
  DETERMINER("DT"),

  // EX Existential there
  EXISTENTIAL_THERE("EX"),

  // FW Foreign word
  FOREIGN_WORD("FW"),

  // IN Preposition or subordinating conjunction
  CONJUNCTION_SUBORDINATING("IN"),

  // JJ Adjective
  ADJECTIVE("JJ"),

  // JJR Adjective, comparative
  ADJECTIVE_COMPARATIVE(ADJECTIVE + "R"),

  // JJS Adjective, superlative
  ADJECTIVE_SUPERLATIVE(ADJECTIVE + "S"),

  // LS List item marker
  LIST_ITEM_MARKER("LS"),

  // MD Modal
  VERB_MODAL("MD"),

  // NN Noun, singular or mass
  NOUN("NN"),

  // NNS Noun, plural
  NOUN_PLURAL(NOUN + "S"),

  // NNP Proper noun, singular
  NOUN_PROPER_SINGULAR(NOUN + "P"),

  // NNPS Proper noun, plural
  NOUN_PROPER_PLURAL(NOUN + "PS"),

  // PDT Predeterminer
  PREDETERMINER("PDT"),

  // POS Possessive ending
  POSSESSIVE_ENDING("POS"),

  // PRP Personal pronoun
  PRONOUN_PERSONAL("PRP"),

  // PRP$ Possessive pronoun
  PRONOUN_POSSESSIVE("PRP$"),

  // RB Adverb
  ADVERB("RB"),

  // RBR Adverb, comparative
  ADVERB_COMPARATIVE(ADVERB + "R"),

  // RBS Adverb, superlative
  ADVERB_SUPERLATIVE(ADVERB + "S"),

  // RP Particle
  PARTICLE("RP"),

  // SYM Symbol
  SYMBOL("SYM"),

  // TO to
  TO("TO"),

  // UH Interjection
  INTERJECTION("UH"),

  // VB Verb, base form
  VERB("VB"),

  // VBD Verb, past tense
  VERB_PAST_TENSE(VERB + "D"),

  // VBG Verb, gerund or present participle
  VERB_PARTICIPLE_PRESENT(VERB + "G"),

  // VBN Verb, past participle
  VERB_PARTICIPLE_PAST(VERB + "N"),

  // VBP Verb, non-3rd person singular present
  VERB_SINGULAR_PRESENT_NONTHIRD_PERSON(VERB + "P"),

  // VBZ Verb, 3rd person singular present
  VERB_SINGULAR_PRESENT_THIRD_PERSON(VERB + "Z"),

  // WDT Wh-determiner
  DETERMINER_WH("W" + DETERMINER),

  // WP Wh-pronoun
  PRONOUN_WH("WP"),

  // WP$ Possessive wh-pronoun
  PRONOUN_POSSESSIVE_WH("WP$"),

  // WRB Wh-adverb
  ADVERB_WH("W" + ADVERB);

  private final String tag;

  private PartOfSpeech(final String tag) {
    this.tag = tag;
  }

  /**
   * Returns the encoding for this part-of-speech.
   *
   * @return A string representing a Penn Treebank encoding for an English part-of-speech.
   */
  @Override
  public String toString() {
    return getTag();
  }

  /**
   * Returns the encoding for this part-of-speech.
   *
   * @return A string representing a Penn Treebank encoding for an English part-of-speech.
   */
  public String getTag() {
    return tag;
  }

  public static PartOfSpeech get(final String value) {
    for (final PartOfSpeech v : values()) {
      if (value.equals(v.getTag())) {
        return v;
      }
    }
    throw new IllegalArgumentException("Unknown part of speech: '" + value + "'.");
  }
}
