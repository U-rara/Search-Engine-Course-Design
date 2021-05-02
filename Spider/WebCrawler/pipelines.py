# Define your item pipelines here
#
# Don't forget to add your pipeline to the ITEM_PIPELINES setting
# See: https://docs.scrapy.org/en/latest/topics/item-pipeline.html


# useful for handling different item types with a single interface
import os
from time import sleep

import pymysql
import re
from pyhanlp import HanLP
from nltk import SnowballStemmer


class WebcrawlerPipeline:
    def open_spider(self, spider):
        self.index = 1
        self.connection = pymysql.connect(
            host='sh-cynosdbmysql-grp-fstsluyi.sql.tencentcdb.com',
            port=28479,
            user='root',
            password='13211X@@',
            db='search',
            charset='utf8'
        )
        self.cur = self.connection.cursor()
        self.cur.execute(
            """
            create table if not exists news (
             id  integer primary key auto_increment,
             language varchar(10),
             title text,
             url text,
             content mediumtext,
             processed_content mediumtext
             )  
            """
        )
        print('爬取开始...')

    def process_item(self, item, spider):
        print('正在提取网页{}'.format(self.index))

        if not check_item(item):
            return

        print('正在预处理网页{}'.format(self.index))

        process_content(item)

        write_item(self, item)

        save_item(self, item)

        print('网页{}处理完成,已保存文本并写入数据库\n\n'.format(self.index))

        self.index = self.index + 1
        return item

    def close_spider(self, spider):
        print('爬取结束，共爬取网页{}个'.format(self.index))


def process_content(item):
    # 对中文文档进行分词
    # 对英文文档进行词干提取
    item['processed_content'] = []
    if item['language'] == 'ZH':
        content = item['content']
        # 预处理
        content = ''.join(content).strip().replace(" ", "").split()[0]
        # 去掉正文中引用的图片
        content = re.sub(r'(https|http)?:\/\/(\w|\.|\/|\?|\=|\&|\%)*\b', '', content, flags=re.MULTILINE)
        content = re.sub(r'编者按.*发布。|编者按：', '', content)
        content = re.sub(r'\W+', '', content).replace("_", '')

        # 分词：基于随机条件场算法
        crf_segment = HanLP.newSegment("crf")
        text = crf_segment.segment(content)

        # 删除停用词
        stopwords = []
        with open('stopwords_zh.txt', encoding='utf-8') as file:
            stopwords = file.read()
        for word in text:
            if word not in stopwords:
                item['processed_content'].append(word)

    elif item['language'] == 'EN':

        stopwords = []
        with open('stopwords_en.txt', encoding='utf-8') as file:
            stopwords = file.read()

        text = item['content']
        text = text.lower()

        # 选择语言
        stemmer = SnowballStemmer("english")
        # 每个单词逐一提取
        for word in re.findall(r"\w+(?:[-']\w+)*|'|[-.(]+|\S\w*", text):
            if word not in stopwords:
                # 提取词根
                item['processed_content'].append(stemmer.stem(word))
        pass


def check_item(item):
    title = item['title']
    url = item['url']
    content = item['content']
    print('文本长度:{}'.format(len(content)))
    if title is None or url is None or content is None or len(content) < 100:
        return False
    else:
        return True


def write_item(self, item):
    origin_file_name = 'News_{}_Org'.format(self.index)
    target_file_name = 'News_{}'.format(self.index)
    if item['language'] == 'ZH':
        origin_file_name = origin_file_name + '_C.txt'
        target_file_name = target_file_name + '_C.txt'
    elif item['language'] == 'EN':
        origin_file_name = origin_file_name + '_E.txt'
        target_file_name = target_file_name + '_E.txt'

    folder = os.path.exists('./data')
    if not folder:
        os.makedirs('./data')

    with open('./data/' + origin_file_name, 'w', encoding='utf-8') as file:
        file.write('标题:\n' + item['title'])
        file.write('\n\n链接:\n' + item['url'])
        file.write('\n\n内容:\n' + item['content'])
        file.close()

    with open('./data/' + target_file_name, 'w', encoding='utf-8') as file:
        processed_content = (item['processed_content'])
        for word in processed_content:
            file.write(word + '\n')
        file.close()


def save_item(self, item):
    processed_content = ','.join([str(x) for x in item['processed_content']])
    sql = "select count(*) from news where url = '%s' " % (item['url'])
    self.cur.execute(sql)
    results = self.cur.fetchall()
    if results[0][0] != 0:
        print('重复爬取，跳过')
        return
    try:
        # 执行sql语句
        sql = """insert into news(language,title,url,content,processed_content) values('%s','%s','%s','%s','%s') """ % (
            item['language'], item['title'], item['url'], item['content'].replace('\'', ''), processed_content)
        # print(sql)
        self.cur.execute(sql)
        # 提交到数据库执行
        self.connection.commit()
    except:
        # 发生错误时回滚
        self.connection.rollback()
