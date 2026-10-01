import logging
import os
from importlib.metadata import PackageNotFoundError, version

from flask import Blueprint, jsonify, request

from service.splitter_service import segment_slds


MAX_BATCH = int(os.environ.get("MAX_BATCH", "1000"))
MAX_SLD_LEN = 253


try:
    MODEL_VERSION = version("dksplit")
except PackageNotFoundError:
    MODEL_VERSION = "unknown"


logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s %(levelname)s %(message)s",
)

log = logging.getLogger("splitter")


splitter_bp = Blueprint(
    "splitter",
    __name__,
)


def _bad_request(message):
    return jsonify({"error": message}), 400


@splitter_bp.post("/segment")
def segment():
    body = request.get_json(silent=True)

    if not isinstance(body, dict) or not isinstance(body.get("items"), list):
        return _bad_request(
            'body must be {"items": [{"id": ..., "sld": "..."}]}'
        )

    items = body["items"]

    if not items:
        return jsonify(
            {
                "modelVersion": MODEL_VERSION,
                "results": [],
            }
        )

    if len(items) > MAX_BATCH:
        return _bad_request(
            f"too many items (max {MAX_BATCH})"
        )

    domain_ids = []
    slds = []

    for i, item in enumerate(items):

        if not isinstance(item, dict) or item.get("domainId") is None:
            return _bad_request(
                f"items[{i}] needs a domainId"
            )

        domain_name = item.get("domainName")

        if not isinstance(domain_name, str) or not domain_name.strip():
            return _bad_request(
                f"items[{i}].domainName must be a non-empty string"
            )

        sld = item.get("sld")

        if not isinstance(sld, str) or not sld.strip():
            return _bad_request(
                f"items[{i}].sld must be a non-empty string"
            )

        if len(sld) > MAX_SLD_LEN:
            return _bad_request(
                f"items[{i}].sld is too long"
            )

        domain_ids.append(item["domainId"])
        slds.append(sld.strip())

    try:
        words_per_sld = segment_slds(slds)

    except Exception:
        log.exception(
            "segmentation failed for a batch of %d",
            len(slds),
        )

        return jsonify(
            {"error": "segmentation failed"}
        ), 500

    results = [
        {
            "domainId": domain_id,
            "keywords": words,
            "sld": sld,
        }
        for domain_id, words, sld in zip(
            domain_ids,
            words_per_sld,
            slds,
        )
    ]

    return jsonify(
        {
            "modelVersion": MODEL_VERSION,
            "results": results,
        }
    )


@splitter_bp.get("/health")
def health():
    return jsonify(
        {
            "status": "ok",
            "modelVersion": MODEL_VERSION,
        }
    )