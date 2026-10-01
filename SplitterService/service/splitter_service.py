import re

import dksplit


TOKEN_RE = re.compile(r"[a-z]+|[0-9]+")


def _split_words(words):
    """Run the DKSplit model on a list of plain lowercase words."""
    out = dksplit.split_batch(words)

    if len(out) != len(words):
        raise RuntimeError(
            f"model returned {len(out)} results for {len(words)} inputs"
        )

    return out


def segment_slds(slds):
    """Return one list of keywords per SLD, in the same order as the input."""

    per_sld_tokens = []

    for sld in slds:
        if sld.startswith("xn--"):
            per_sld_tokens.append(None)
        else:
            per_sld_tokens.append(
                TOKEN_RE.findall(sld.lower())
            )

    letter_tokens = sorted(
        {
            token
            for tokens in per_sld_tokens
            if tokens
            for token in tokens
            if token.isalpha()
        }
    )

    split_map = (
        dict(zip(letter_tokens, _split_words(letter_tokens)))
        if letter_tokens
        else {}
    )

    results = []

    for sld, tokens in zip(slds, per_sld_tokens):

        if tokens is None:
            results.append([sld.lower()])
            continue

        words = []

        for token in tokens:
            words.extend(
                split_map.get(token, [token])
            )

        results.append(words)

    return results