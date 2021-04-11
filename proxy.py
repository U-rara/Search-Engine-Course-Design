import requests


def get_proxy():
    return requests.get("http://118.24.52.95:5010/get/").json()


def delete_proxy(proxy):
    requests.get("http://118.24.52.95:5010/delete/?proxy={}".format(proxy))


if __name__ == '__main__':
    url = []

    for i in range(1000):

        proxy = get_proxy().get("proxy")
        print(proxy)

        r = requests.get('https://36kr.com/information/web_news/latest', proxies={"http": "http://{}".format(proxy)})  # 豆瓣首页
        print(r.status_code)
        if r.status_code == 200:
            url.append(""""http": "http://{}".format(proxy)}""")

    print(url)