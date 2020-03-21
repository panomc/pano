# Pano

Pano is an advanced web platform that comfort your Minecraft server's website needs.

#### Project Status
Pano is still under heavy development. There can be breaking changes but we're trying to keep them as minimum as possible.

#### Prerequisites
JDK 8+ <br>
MySQL v5+ / MariaDB <br>
Docker & Docker Compose (optional)

## Development
#### Getting Started
Clone this repository.

```bash
git clone --recursive https://gitlab.com/defio-workshop/pano/pano-web-platform.git
cd pano-web-platform
```

##### Compile & Run

```bash
./gradlew vertxRun
```

##### Running Debug with Gradle

```bash
./gradlew vertxDebug
```

### Using Docker (recommended)

```bash
docker-compose up
```

##### MySQL Default Values

```bash
database: pano
root password: pano
```

## Contributing
Merge requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

## License
This project is licensed under the GNU GPLv3 License - see the [LICENSE](LICENSE) file for details