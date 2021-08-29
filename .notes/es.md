嵌入式查询、排序、分页、范围、高亮
GET product/_search
```json
{
  "query": {
    "bool": {
      "filter": {
        "nested": {
          "path": "attrs",
          "query": {
            "bool": {
              "must": [
                {
                  "term": {
                    "attrs.attrId": {
                      "value": "1"
                    }
                  }
                },
                {
                  "terms": {
                    "attrs.attrValue": [
                      "aa"
                    ]
                  }
                },
                {
                  "range": {
                    "skuPrice": {
                      "gte": 0,
                      "lte": 1000
                    }
                  }
                }
              ]
            }
          }
        }
      }
    }
  },
  "sort": [
    {
      "skuId": {
        "order": "desc"
      }
    }
  ],
  "from": 0,
  "size": 20,
  "highlight": {
    "fields": {
      "skuTitle": {}
    },
    "pre_tags": "<b style='color:red' >",
    "post_tags": "</b>"
  }
}
```
模糊匹配、过滤
GET product/_search
```json
{
  "query": {
    "bool": {
      "must": [
        {
          "match": {
            "skuTitle": "华为"
          }
        }
      ],
      "filter": [
        {
          "term": {
            "catalogId": "225"
          }
        },
        {
        "terms": {
          "brandId": [
            "1",
            "2",
            "9"
          ]
        }
      }
      ]
    }
  }
}
```