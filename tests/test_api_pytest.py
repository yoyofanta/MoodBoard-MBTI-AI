# tests/test_api_pytest.py
# 使用 pytest 执行 MoodBoard × MBTI 接口自动化测试
# 执行方式：
# pytest tests/test_api_pytest.py --html=tests/reports/api_report.html --self-contained-html

import sys
from pathlib import Path

# 让 pytest 可以导入同目录下的 api_test.py
CURRENT_DIR = Path(__file__).resolve().parent
sys.path.append(str(CURRENT_DIR))

from api_test import ApiTestClient


client = ApiTestClient()


def test_01_register():
    client.test_register()


def test_02_login():
    client.test_login()


def test_03_login_wrong_password():
    client.test_login_wrong_password()


def test_04_get_profile():
    client.test_get_profile()


def test_05_save_profile():
    client.test_save_profile()


def test_06_memory_chat():
    client.test_memory_chat()


def test_07_get_memory():
    client.test_get_memory()


def test_08_knowledge_search():
    client.test_knowledge_search()


def test_09_knowledge_ask():
    client.test_knowledge_ask()


def test_10_location_tool():
    client.test_location_tool()


def test_11_agent_roundtable():
    client.test_agent_roundtable()


def test_12_agent_roundtable_less_than_two():
    client.test_agent_roundtable_less_than_two()


def test_13_clear_memory():
    client.test_clear_memory()